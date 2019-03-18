package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public abstract class Tile implements WorldObject {
	
	/*
		So. The client, in terms of properties, doesn't have to worry about tile interaction properties. It always sends a request to the server when interactions should occur.
		The server, in turn, doesn't have to worry about animation or rendering properties.
		
		So, it seems like we have a situation where there are ServerProperties and ClientProperties, and some property types are both.
		
		This could mean that I should just take the client and server properties out of the main game module and into their own respective modules. But that would mean they can never be referenced in the main package, and I like how the property types are all given in the same place, the TileType class. So maybe I can leave the shell there, and just separate the stuff that actually does the heavy lifting..?
		
		 Hey, that actually gives me an idea: What if the property types/classes I specify in the TileType class weren't actually the objects that I used for the tile behaviors, in the end? What if they were just markers, and the actual property instances were instantiated later? For entities, they would obviously be instantiated on entity creation, but since there are so many tiles loaded at one time, we have to do tiles different...
		 Hey, I know: how about we have a tile property instance fetcher, that creates all the actual tile property instances within the respective client/server modules, with the right classes, based on the given main property class? That could work! Then, whenever a tile property was asked for, it would fetch it from the fetcher, given the TileType and property class/type. With entities, each would simply have their own list, their own fetcher.
		 The fetchers would be created in ClientCore and ServerCore, more or less. or maybe the Worlds, since the WorldManager class would have to have a way to fetch a property instance given a TileType and and Property class/type. For entities, the fetcher would be given the entity instance too. Or maybe each entity would just already have its properties. Yeah that'll probably be the case.
		 
		 So! End result. Actual property instances are created in Client/Server individually, not in the TileType enum. That is only where the basic templates go. The is a fetcher that can take a property type instance, and return a completed property instance of that type.
		 Note, we might end up having a property type enum as well as a tile type enum...
	 */
	
	public static final int RESOLUTION = 32;
	public static final int SCALE = 4;
	public static final int SIZE = RESOLUTION * SCALE;
	
	private TileStack tileStack;
	private final Object dataLock = new Object();
	
	@NotNull private final Level level;
	final int x, y;
	final EnumMap<TileTypeEnum, SerialMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	Tile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types) {
		this.level = level;
		this.x = x;
		this.y = y;
		setTileStack(makeStack(types));
	}
	
	abstract TileStack makeStack(@NotNull TileTypeEnum[] types);
	
	void setTileStack(TileStack stack) { this.tileStack = stack; }
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	
	public TileType getType() { return tileStack.getTopLayer(); }
	public TileStack getTypeStack() { return tileStack; }
	
	public SerialMap getDataMap() { return getDataMap(getType().getTypeEnum()); }
	public SerialMap getDataMap(TileType tileType) { return getDataMap(tileType.getTypeEnum()); }
	public SerialMap getDataMap(TileTypeEnum tileType) { return dataMaps.computeIfAbsent(tileType, k -> new SerialMap()); }
	
	
	public HashSet<Tile> getAdjacentTiles(boolean includeCorners) {
		if(includeCorners)
			return level.getAreaTiles(x, y, 1, false);
		else {
			HashSet<Tile> tiles = new HashSet<>();
			if(x > 0) tiles.add(level.getTile(x-1, y));
			if(y < level.getHeight()-1) tiles.add(level.getTile(x, y+1));
			if(x < level.getWidth()-1) tiles.add(level.getTile(x+1, y));
			if(y > 0) tiles.add(level.getTile(x, y-1));
			tiles.remove(null);
			return tiles;
		}
	}
	
	@Override
	public String toString() { return getType().getName()+' '+getClass().getSimpleName(); }
	
	public String toLocString() { return (x-level.getWidth()/2)+","+(y-level.getHeight()/2)+" ("+toString()+')'; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return Point.javaPointHashCode(x, y) + level.getLevelId() * 17; }
	
	// I can use the string encoder and string parser in MyUtils to encode the tile data in a way so that I can always re-parse the encoded array. I can use this internally to, with other things, whenever I need to encode a list of objects and don't want to worry about finding the delimiter symbol in string somewhere I don't expect.
	
	public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		
		private TileData() { this((int[])null, null); }
		private TileData(int[] typeOrdinals, String[] data) {
			this.typeOrdinals = typeOrdinals;
			this.data = data;
		}
		public TileData(Tile tile) {
			synchronized (tile.dataLock) {
				TileTypeEnum[] tileTypes = tile.getTypeStack().getEnumTypes();
				typeOrdinals = new int[tileTypes.length];
				for(int i = 0; i < tileTypes.length; i++) {
					TileTypeEnum type = tileTypes[i];
					typeOrdinals[i] = type.ordinal();
				}
				
				this.data = new String[tileTypes.length];
				for(int i = 0; i < data.length; i++)
					data[i] = tile.dataMaps.get(tileTypes[i]).serialize();
			}
		}
		
		public TileData(Version dataVersion, String tileData) {
			String[] all = MyUtils.parseLayeredString(tileData);
			data = Arrays.copyOfRange(all, 1, all.length);
			
			typeOrdinals = ArrayUtils.mapArray(all[0].split(","), int.class, int[].class, Integer::parseInt);
		}
		
		public String serialize() {
			String[] all = new String[data.length+1];
			System.arraycopy(data, 0, all, 1, data.length);
			
			all[0] = ArrayUtils.arrayToString(typeOrdinals, ",");
			
			return MyUtils.encodeStringArray(all);
		}
		
		public TileTypeEnum[] getTypes() { return getTypes(typeOrdinals); }
		public static TileTypeEnum[] getTypes(int[] typeOrdinals) {
			TileTypeEnum[] types = new TileTypeEnum[typeOrdinals.length];
			for(int i = 0; i < types.length; i++) {
				types[i] = TileTypeEnum.value(typeOrdinals[i]);
			}
			return types;
		}
		
		public SerialMap[] getDataMaps() { return getDataMaps(data); }
		public static SerialMap[] getDataMaps(String[] data) {
			SerialMap[] maps = new SerialMap[data.length];
			for(int i = 0; i < data.length; i++)
				maps[i] = SerialMap.deserialize(data[i], TileCacheTag.class);
			return maps;
		}
		
	}
	
	public static class TileTag implements Tag<Tile> {
		public final int x;
		public final int y;
		public final int levelId;
		
		private TileTag() { this(0, 0, 0); }
		public TileTag(Tile tile) { this(tile.x, tile.y, tile.getLevel().getLevelId()); }
		public TileTag(int x, int y, int levelId) {
			this.x = x;
			this.y = y;
			this.levelId = levelId;
		}
		
		@Override
		public Tile getObject(WorldManager world) {
			Level level = world.getLevel(levelId);
			if(level != null)
				return level.getTile(x, y);
			return null;
		}
	}
	
	@Override
	public Tag<Tile> getTag() { return new TileTag(this); }
}
