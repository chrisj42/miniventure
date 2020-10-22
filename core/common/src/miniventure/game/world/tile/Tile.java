package miniventure.game.world.tile;

import java.util.HashSet;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueAction;
import miniventure.game.util.pool.RectPool;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	
	public static final int RESOLUTION = 16;
	public static final int SCALE = 4;
	public static final int SIZE = RESOLUTION * SCALE;
	
	private TileStack<?> tileStack;
	
	@NotNull private final Level level;
	public final int x, y;
	// final EnumMap<TileTypeEnum, SerialMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	Tile(@NotNull Level level, int x, int y) {
		this.level = level;
		this.x = x;
		this.y = y;
		tileStack = makeStack();
	}
	
	abstract TileStack<?> makeStack();
	
	public void setStack(TileTypeInfo[] stackInfo) {
		tileStack.setStack(getWorld(), stackInfo);
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return RectPool.POOL.obtain(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return VectorPool.POOL.obtain(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	
	public TileType getType() { return tileStack.getTopLayer(); }
	public TileStack<?> getTypeStack() { return tileStack; }
	
	// public SerialMap getDataMap(TileType tileType) { return getDataMap(tileType.getTypeEnum()); }
	@NotNull
	public TileTypeDataMap getDataMap(TileTypeEnum tileType) {
		TileTypeDataMap map = tileStack.getDataMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			MyUtils.error("ERROR: tile " + toLocString() + " came back with a null data map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = getWorld().getTileType(tileType).createDataMap();
		}
		return map;
	}
	
	/*@NotNull
	public TileDataTag.TileDataCache getDataMap(TileTypeEnum tileType) {
		TileDataMap map = tileStack.getCacheMap(tileType);
		// should never happen, especially with the new synchronization. But this will stay, just in case.
		if(map == null) {
			MyUtils.error("ERROR: tile " + toLocString() + " came back with a null cache map for tiletype " + tileType + "; stack: " + tileStack.getDebugString(), true, true);
			map = new TileDataMap();
		}
		return map;
	}*/
	
	public void forAdjacentTiles(boolean includeCorners, ValueAction<Tile> action) {
		if(includeCorners)
			level.forAreaTiles(x, y, 1, false, action);
		else {
			// HashSet<Tile> tiles = new HashSet<>();
			if(x > 0) action.act(level.getTile(x-1, y));
			if(y < level.getHeight()-1) action.act(level.getTile(x, y+1));
			if(x < level.getWidth()-1) action.act(level.getTile(x+1, y));
			if(y > 0) action.act(level.getTile(x, y-1));
			// tiles.remove(null);
			// return tiles;
		}
	}
	
	public HashSet<Tile> getAdjacentTiles(boolean includeCorners) {
		HashSet<Tile> tiles = new HashSet<>();
		forAdjacentTiles(includeCorners, tiles::add);
		return tiles;
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
	public int hashCode() { return Point.javaPointHashCode(x, y) + level.getLevelId().hashCode() * 17; }
	
	public static class TileTag implements Tag<Tile> {
		public final int x;
		public final int y;
		public final LevelId levelId;
		
		private TileTag() { this(0, 0, null); }
		public TileTag(Tile tile) { this(tile.x, tile.y, tile.getLevel().getLevelId()); }
		public TileTag(int x, int y, LevelId levelId) {
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
