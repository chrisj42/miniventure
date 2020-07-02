package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashSet;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileCacheTag.TileDataCache;
import miniventure.game.world.tile.TileDataTag.TileDataMap;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.worldgen.level.ProtoTile;

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
	
	private final EnumMap<TileLayer, TileType> typeStack;
	private TileType topCache;
	
	@NotNull private final Level level;
	public final int x, y;
	// final EnumMap<TileTypeEnum, SerialMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	
	// create
	Tile(@NotNull Level level, @NotNull ProtoTile tile) {
		this.level = level;
		this.x = tile.pos.x;
		this.y = tile.pos.y;
		typeStack = new EnumMap<>(TileLayer.class);
		setTypeStack(tile.getTypes(), false);
	}
	// the TileType array is ALWAYS expected in order of bottom to top.
	Tile(@NotNull Level level, int x, int y, @NotNull TileData data) {
		this.level = level;
		this.x = x;
		this.y = y;
		typeStack = new EnumMap<>(TileLayer.class);
		setTypeStack(data.types, false);
	}
	
	void setTypeStack(TileTypeEnum[] types) { setTypeStack(types, true); }
	void setTypeStack(TileTypeEnum[] types, boolean resetData) {
		for(TileTypeEnum type: types) {
			if(type != null)
				typeStack.put(type.layer, level.getWorld().getTileType(type));
		}
		refreshTopCache();
		if(resetData)
			level.resetTileData(this);
	}
	
	private void refreshTopCache() {
		for(int i = TileLayer.values.length-1; i >= 0; i--) {
			if(getType(TileLayer.values[i]) != null)
				topCache = getType(TileLayer.values[i]);
		}
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	void setType(TileTypeEnum type) { setType(level.getWorld().getTileType(type)); }
	void setType(TileType type) {
		typeStack.put(type.getTypeEnum().layer, type);
		refreshTopCache();
	}
	
	public TileType getType() {
		if(topCache == null) refreshTopCache();
		return topCache;
	}
	public TileType getType(TileLayer layer) { return typeStack.get(layer); }
	public EnumMap<TileLayer, ? extends TileType> getTypeStack() { return typeStack; }
	public TileType getUnderType() {
		for(int i = topCache.getTypeEnum().layer.ordinal(); i >= 0; i--) {
			TileType curType = getType(TileLayer.values[i]);
			if(curType == null) continue;
			TileTypeEnum under = curType.getTypeEnum().getUnderType();
			if(under != null)
				return getWorld().getTileType(under);
		}
		return getWorld().getTileType(TileTypeEnum.HOLE);
	}
	
	// public SerialMap getDataMap(TileType tileType) { return getDataMap(tileType.getTypeEnum()); }
	/*@NotNull
	public TileDataMap getDataMap(TileTypeEnum tileType) {
		TileDataMap map = typeStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			MyUtils.error("ERROR: tile " + toLocString() + " came back with a null data map for tiletype " + tileType + "; stack: " + typeStack.getDebugString(), true, true);
			map = new TileDataMap();
		}
		return map;
	}*/
	
	/*@NotNull
	public TileCacheTag.TileDataCache getCacheMap(TileTypeEnum tileType) {
		TileDataCache map = typeStack.getCacheMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			MyUtils.error("ERROR: tile " + toLocString() + " came back with a null cache map for tiletype " + tileType + "; stack: " + typeStack.getDebugString(), true, true);
			map = new TileDataCache();
		}
		return map;
	}*/
	
	
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
	
	public String toLocString() { return x+","+y+" ("+toString()+')'; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return x * level.getHeight() + y; }
	
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
