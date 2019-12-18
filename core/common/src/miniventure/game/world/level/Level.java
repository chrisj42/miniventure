package miniventure.game.world.level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.Point;
import miniventure.game.world.Taggable;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Level implements Taggable<Level> {
	
	// public static final int X_LOAD_RADIUS = 4, Y_LOAD_RADIUS = 2;
	
	private final int levelId;
	private final int width;
	private final int height;
	
	@NotNull private final WorldManager world;
	@NotNull private Tile[][] tiles;
	// final SynchronizedAccessor<Map<Point, Chunk>> loadedChunks = new SynchronizedAccessor<>(Collections.synchronizedMap(new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2)));
	//private int tileCount;
	private int mobCount;
	
	// this is to track when entities go in and out of chunks
	// final HashMap<Entity, Point> entityChunks = new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2);
	
	/** @noinspection FieldCanBeLocal*/
	private int mobCap = 80;
	
	@FunctionalInterface
	public interface TileMaker {
		Tile get(Level level, int x, int y, TileTypeEnum[] types);
	}
	
	@FunctionalInterface
	public interface TileLoader {
		Tile get(Level level, int x, int y, TileTypeEnum[] types, SerialMap[] dataMaps);
	}
	
	private Level(@NotNull WorldManager world, int levelId, int width, int height) {
		this.world = world;
		this.levelId = levelId;
		this.width = width;
		this.height = height;
		
		tiles = new Tile[width][height];
	}
	
	protected Level(@NotNull WorldManager world, int levelId, @NotNull TileTypeEnum[][][] tileTypes, @NotNull TileMaker tileFetcher) {
		this(world, levelId, tileTypes.length, tileTypes.length == 0 ? 0 : tileTypes[0].length);
		
		GameCore.debug(world.getClass().getSimpleName()+": fetching level "+levelId+" initial tile data...");
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = tileFetcher.get(this, x, y, tileTypes[x][y]);
		
		GameCore.debug(world.getClass().getSimpleName()+": tile data initialized.");
	}
	
	protected Level(@NotNull WorldManager world, int levelId, TileData[][] tileData, TileLoader tileFetcher) {
		this(world, levelId, tileData.length, tileData.length == 0 ? 0 : tileData[0].length);
		
		GameCore.debug(world.getClass().getSimpleName()+": loading level "+levelId+" tile data...");
		for(int x = 0; x < tileData.length; x++) {
			for(int y = 0; y < tileData[x].length; y++) {
				TileData data = tileData[x][y];
				tiles[x][y] = tileFetcher.get(this, x, y, data.getTypes(), data.getDataMaps());
			}
		}
		GameCore.debug(world.getClass().getSimpleName()+": tile data loaded.");
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getLevelId() { return levelId; }
	@NotNull public WorldManager getWorld() { return world; }
	public int getMobCap() { return mobCap; }
	public int getMobCount() { return mobCount; }
	public abstract int getEntityCount();
	
	public abstract Set<? extends Entity> getEntities();
	
	public TileData[][] getTileData(boolean save) {
		TileData[][] data = new TileData[width][height];
		
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				data[x][y] = new TileData(tiles[x][y], save);
		
		return data;
	}
	
	// protected int getLoadedChunkCount() { return loadedChunks.get(Map::size); }
	// protected Chunk getLoadedChunk(Point p) { return loadedChunks.get(chunks -> chunks.get(p)); }
	// public boolean isChunkLoaded(Point p) { return loadedChunks.get(chunks -> chunks.containsKey(p)); }
	// protected void putLoadedChunk(Point p, Chunk c) { loadedChunks.access(chunks -> chunks.put(p, c)); }
	
	/*public synchronized void entityMoved(Entity entity) {
		Point prevChunk = entityChunks.get(entity);
		Point curChunk = !this.equals(entity.getLevel()) ? null : Chunk.getCoords(entity.getCenter());
		
		if(Objects.equals(prevChunk, curChunk)) return;
		//System.out.println(world+" entity "+entity+" changed chunk, from "+prevChunk+" to "+curChunk);
		
		if(curChunk == null)
			entityChunks.remove(entity);
		else
			entityChunks.put(entity, curChunk);
		
		if(!world.isKeepAlive(entity)) return;
		
		//System.out.println("keep alive changed chunk on "+world);
		
		pruneLoadedChunks();
		
		if(curChunk != null) {
			// load any new chunks surrounding the given entity
			for(Point p : getAreaChunkCoords(entity.getCenter(), X_LOAD_RADIUS, Y_LOAD_RADIUS, false, true)) {
				//System.out.println("loading chunk on "+world+" at "+p);
				loadChunk(p);
			}
		}
	}*/
	
	/*void pruneLoadedChunks() {
		// check for any chunks that no longer need to be loaded
		Array<Point> chunkCoords = new Array<>(loadedChunks.<Point[]>get(chunks -> chunks.keySet().toArray(new Point[chunks.size()])));
		for(WorldObject obj: world.getKeepAlives(this)) // remove loaded chunks in radius
			chunkCoords.removeAll(getAreaChunkCoords(obj.getCenter(), X_LOAD_RADIUS+1, Y_LOAD_RADIUS+1, true, false), false);
		
		// chunkCoords now contains all chunks which have no nearby keepAlive object, so they should be unloaded.
		for(Point chunkCoord: chunkCoords) {
			Chunk chunk = getLoadedChunk(chunkCoord);
			if(chunk == null) continue; // shouldn't happen, but just in case
			
			// decrease tile count by number of tiles in chunk
			//tileCount -= chunk.width * chunk.height;
			// get all entities in the chunk, and remove them from the game (later they'll be saved instead)
			Array<Entity> entities = getOverlappingEntities(new Rectangle(chunkCoord.x * Chunk.SIZE, chunkCoord.y * Chunk.SIZE, chunk.width, chunk.height));
			for(Entity e: entities)
				e.remove();
			
			// I don't think I have to worry about the tiles...
			// now, remove the chunk from the set of loaded chunks
			unloadChunk(chunkCoord);
		}
	}*/
	
	
	public void update(float delta) {
		int mobs = 0;
		for(Entity e: getEntities()) {
			e.update(delta);
			if(e.isMob())
				mobs++;
		}
		this.mobCount = mobs;
	}
	
	public Tile getTile(Rectangle rect) { return getTile(rect.getCenter(new Vector2())); }
	public Tile getTile(Vector2 pos) { return getTile(pos.x, pos.y); }
	public Tile getTile(float x, float y) {
		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			return null;
			// System.err.println("out of bounds tile request on level "+levelId+" at "+x+','+y+"; using nearest tile");
		
		int xt = (int)x;//MyUtils.clamp((int)x, 0, getWidth()-1);
		int yt = (int)y;//MyUtils.clamp((int)y, 0, getHeight()-1);
		
		return tiles[xt][yt];
		
		// int chunkX = xt / Chunk.SIZE;
		// int chunkY = yt / Chunk.SIZE;
		// xt -= chunkX * Chunk.SIZE;
		// yt -= chunkY * Chunk.SIZE;
		
		// Chunk chunk = getLoadedChunk(new Point(chunkX, chunkY));
		// if(chunk != null)
		// return (Tile) chunk.getTile(xt, yt);
		
		// return null;
	}
	
	public Tile getClosestTile(Rectangle rect) { return getClosestTile(rect.getCenter(new Vector2())); }
	public Tile getClosestTile(Vector2 center) { return getClosestTile(center.x, center.y); }
	public Tile getClosestTile(float x, float y) {
		x = MyUtils.clamp(x, 0, getWidth()-1);
		y = MyUtils.clamp(y, 0, getHeight()-1);
		return getTile(x, y);
	}
	
	public HashSet<Tile> getAreaTiles(Point tilePos, int radius, boolean includeCenter) { return getAreaTiles(tilePos.x, tilePos.y, radius, includeCenter); }
	public HashSet<Tile> getAreaTiles(int x, int y, int radius, boolean includeCenter) {
		
		HashSet<Tile> tiles = new HashSet<>();
		for(int xo = Math.max(0, x-radius); xo <= Math.min(getWidth()-1, x+radius); xo++)
			for(int yo = Math.max(0, y-radius); yo <= Math.min(getHeight()-1, y+radius); yo++)
				tiles.add(getTile(xo, yo));
		tiles.remove(null);
		
		if(!includeCenter)
			tiles.remove(getTile(x, y));
		
		return tiles;
	}
	
	private Array<Point> getOverlappingTileCoords(Rectangle rect) {
		Array<Point> overlappingTiles = new Array<>();
		int minX = Math.max(0, (int) rect.x);
		int minY = Math.max(0, (int) rect.y);
		int maxX = Math.min(getWidth(), (int) (rect.x + rect.width));
		int maxY = Math.min(getHeight(), (int) (rect.y + rect.height));
		
		for(int x = minX; x <= maxX; x++) {
			for (int y = maxY; y >= minY; y--) {
				overlappingTiles.add(new Point(x, y));
			}
		}
		
		return overlappingTiles;
	}
	
	public Array<Tile> getOverlappingTiles(Rectangle rect) {
		Array<Tile> overlappingTiles = new Array<>();
		Array<Point> points = getOverlappingTileCoords(rect);
		
		for(Point p: points) {
			Tile tile = getTile(p.x, p.y);
			if (tile != null)
				overlappingTiles.add(tile);
		}
		
		return overlappingTiles;
	}
	
	/*protected Array<Point> getOverlappingChunks(Rectangle rect) {
		Array<Point> overlappingChunks = new Array<>();
		Array<Point> points = getOverlappingTileCoords(rect);
		
		for(Point p: points) {
			Point chunk = Chunk.getCoords(p);
			if(!overlappingChunks.contains(chunk, false))
				overlappingChunks.add(chunk);
		}
		
		return overlappingChunks;
	}*/
	
	public Array<Entity> getOverlappingEntities(Rectangle rect, Entity... exclude) {
		Array<Entity> overlapping = new Array<>(Entity.class);
		for(Entity entity: getEntities())
			if(entity.getBounds().overlaps(rect))
				overlapping.add(entity);
		
		if(exclude.length > 0)
			overlapping.removeAll(new Array<>(exclude), true); // use ==, not .equals()
		
		return overlapping;
	}
	
	public Array<WorldObject> getOverlappingObjects(Rectangle area) {
		Array<WorldObject> objects = new Array<>(WorldObject.class);
		objects.addAll(getOverlappingTiles(area));
		objects.addAll(getOverlappingEntities(area));
		return objects;
	}
	
	// chunkRadius is in chunks.
	/*public Array<Point> getAreaChunkCoords(Vector2 tilePos, int chunkRadiusX, int chunkRadiusY, boolean loaded, boolean unloaded) {
		return getAreaChunkCoords(tilePos.x, tilePos.y, chunkRadiusX, chunkRadiusY, loaded, unloaded);
	}
	public Array<Point> getAreaChunkCoords(float x, float y, int radiusX, int radiusY, boolean loaded, boolean unloaded) {
		return getAreaChunkCoords(Chunk.getCoord(x), Chunk.getCoord(y), radiusX, radiusY, loaded, unloaded);
	}
	public Array<Point> getAreaChunkCoords(int chunkX, int chunkY, int radiusX, int radiusY, boolean loaded, boolean unloaded) {
		Array<Point> chunkCoords = new Array<>();
		for(int x = chunkX - radiusX; x <= chunkX + radiusX; x++) {
			for (int y = chunkY - radiusY; y <= chunkY + radiusY; y++) {
				if (chunkExists(x, y)) {
					Point p = new Point(x, y);
					boolean isLoaded = isChunkLoaded(p);
					if(loaded && isLoaded || unloaded && !isLoaded)
						chunkCoords.add(new Point(x, y));
				}
			}
		}
		
		return chunkCoords;
	}*/
	
	public ArrayList<Tile> getMatchingTiles(TileTypeEnum type) {
		ArrayList<Tile> matches = new ArrayList<>();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(tiles[x][y].getType().getTypeEnum() == type)
					matches.add(tiles[x][y]);
			}
		}
		
		return matches;
	}
	
	@Nullable
	public Player getClosestPlayer(final Vector2 pos) {
		Array<Player> players = new Array<>();
		for(Entity e: getEntities())
			if(e instanceof Player)
				players.add((Player)e);
		
		if(players.size == 0) return null;
		
		players.sort((p1, p2) -> Float.compare(p1.getCenter().dst(pos), p2.getCenter().dst(pos)));
		
		return players.get(0);
	}
	
	// public boolean chunkExists(int cx, int cy) { return tileExists(cx * Chunk.SIZE, cy * Chunk.SIZE); }
	/*public boolean tileExists(int tx, int ty) {
		if(tx < 0 || ty < 0) return false;
		
		if(tx >= getWidth() || ty >= getHeight())
			return false;
		
		return true;
	}*/
	
	// protected abstract void loadChunk(Point chunkCoord);
	// protected abstract void unloadChunk(Point chunkCoord);
	
	/*public void loadChunk(Chunk newChunk) {
		//tileCount += newChunk.width * newChunk.height;
		putLoadedChunk(new Point(newChunk.chunkX, newChunk.chunkY), newChunk);
	}*/
	
	@Override
	public boolean equals(Object other) { return other instanceof Level && ((Level)other).levelId == levelId; }
	@Override
	public int hashCode() { return levelId; }
	
	@Override
	public String toString() { return getClass().getSimpleName()+"(levelId="+ levelId +')'; }
	
	@Override
	public LevelTag getTag() { return new LevelTag(levelId); }
	
	public static class LevelTag implements Tag<Level> {
		
		private final int levelId;
		
		public LevelTag(int levelId) { this.levelId = levelId; }
		
		@Override
		public Level getObject(WorldManager world) {
			return world.getLevel(levelId);
		}
	}
}
