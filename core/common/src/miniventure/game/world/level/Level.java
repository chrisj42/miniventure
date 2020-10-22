package miniventure.game.world.level;

import java.util.ArrayList;
import java.util.HashSet;

import miniventure.game.util.MyUtils;
import miniventure.game.util.ValueWrapper;
import miniventure.game.util.function.ValueAction;
import miniventure.game.util.pool.RectPool;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.Point;
import miniventure.game.world.Taggable;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.ProtoLevel;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Level implements Taggable<Level> {
	
	// public static final int X_LOAD_RADIUS = 4, Y_LOAD_RADIUS = 2;
	
	private final LevelId levelId;
	private final int width;
	private final int height;
	
	@NotNull private final WorldManager world;
	@NotNull private final Tile[][] tiles;
	// final SynchronizedAccessor<Map<Point, Chunk>> loadedChunks = new SynchronizedAccessor<>(Collections.synchronizedMap(new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2)));
	//private int tileCount;
	private int mobCount;
	
	// this is to track when entities go in and out of chunks
	// final HashMap<Entity, Point> entityChunks = new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2);
	
	/** @noinspection FieldCanBeLocal*/
	private final int mobCap = 80;
	
	/*@FunctionalInterface
	public interface TileMaker {
		Tile get(Level level, int x, int y, TileTypeInfo[] types);
	}
	
	@FunctionalInterface
	public interface TileLoader {
		Tile get(Level level, int x, int y, TileTypeEnum[] types, TileTypeDataMap[] dataMaps);
	}
	
	@FunctionalInterface
	public interface TileFetcher {
		Tile get(Level level, int x, int y);
	}*/
	
	protected Level(@NotNull WorldManager world, LevelId levelId, int width, int height) {
		this.world = world;
		this.levelId = levelId;
		this.width = width;
		this.height = height;
		
		tiles = new Tile[width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				tiles[x][y] = makeTile(x, y);
			}
		}
	}
	
	/*protected Level(@NotNull WorldManager world, LevelId levelId, @NotNull ProtoLevel protoLevel, @NotNull TileMaker tileFetcher) {
		this(world, levelId, protoLevel.width, protoLevel.height);
		
		MyUtils.debug(world.getClass().getSimpleName()+": fetching level "+levelId+" initial tile data...");
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = tileFetcher.get(this, x, y, protoLevel., );
		
		MyUtils.debug(world.getClass().getSimpleName()+": tile data initialized.");
	}
	
	protected Level(@NotNull WorldManager world, LevelId levelId, TileData[][] tileData, TileLoader tileFetcher) {
		this(world, levelId, tileData.length, tileData.length == 0 ? 0 : tileData[0].length);
		
		MyUtils.debug(world.getClass().getSimpleName()+": loading level "+levelId+" tile data...");
		for(int x = 0; x < tileData.length; x++) {
			for(int y = 0; y < tileData[x].length; y++) {
				TileData data = tileData[x][y];
				tiles[x][y] = tileFetcher.get(this, x, y, data.getTypes(), data.getDataMaps(world));
			}
		}
		MyUtils.debug(world.getClass().getSimpleName()+": tile data loaded.");
	}
	
	protected Level(@NotNull WorldManager world, LevelId levelId, int width, int height, TileFetcher tileFetcher) {
		this(world, levelId, width, height);
		MyUtils.debug(world.getClass().getSimpleName()+": loading level "+levelId+" tile placeholders...");
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				tiles[x][y] = tileFetcher.get(this, x, y);
			}
		}
		MyUtils.debug(world.getClass().getSimpleName()+": tile placeholders loaded.");
	}*/
	
	protected abstract Tile makeTile(int x, int y);
	
	protected void setTiles(ProtoLevel protoLevel) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				Tile tile = tiles[x][y];
				tile.setStack(protoLevel.getTile(x, y).getStack());
			}
		}
	}
	protected void setTiles(TileData[][] data) {
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				Tile tile = tiles[x][y];
				tile.setStack(data[x][y].parseStack(world));
			}
		}
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public LevelId getLevelId() { return levelId; }
	@NotNull public WorldManager getWorld() { return world; }
	public int getMobCap() { return mobCap; }
	public int getMobCount() { return mobCount; }
	public abstract int getEntityCount();
	
	public abstract void forEachEntity(ValueAction<Entity> action);
	
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
			Array<Entity> entities = getOverlappingEntities(RectPool.POOL.obtain(chunkCoord.x * Chunk.SIZE, chunkCoord.y * Chunk.SIZE, chunk.width, chunk.height));
			for(Entity e: entities)
				e.remove();
			
			// I don't think I have to worry about the tiles...
			// now, remove the chunk from the set of loaded chunks
			unloadChunk(chunkCoord);
		}
	}*/
	
	
	public void update(float delta) {
		mobCount = 0;
		forEachEntity(e -> {
			e.update(delta);
			if(e.isMob())
				mobCount++;
		});
	}
	
	public Tile getTile(Rectangle rect) { return getTile(rect, false); }
	public Tile getTile(Rectangle rect, boolean free) {
		Tile tile = getTile(rect.getCenter(VectorPool.POOL.obtain()), true);
		if(free) RectPool.POOL.free(rect);
		return tile;
	}
	public Tile getTile(Vector2 pos) { return getTile(pos, false); }
	public Tile getTile(Vector2 pos, boolean free) {
		Tile tile = getTile(pos.x, pos.y);
		if(free) VectorPool.POOL.free(pos);
		return tile;
	}
	public Tile getTile(float x, float y) { return getTile((int)x, (int)y); }
	public Tile getTile(Point pos) { return getTile(pos.x, pos.y); }
	public Tile getTile(int x, int y) {
		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			return null;
			// System.err.println("out of bounds tile request on level "+levelId+" at "+x+','+y+"; using nearest tile");
		
		// int xt = (int)x;//MyUtils.clamp((int)x, 0, getWidth()-1);
		// int yt = (int)y;//MyUtils.clamp((int)y, 0, getHeight()-1);
		
		return tiles[x][y];
		
		// int chunkX = xt / Chunk.SIZE;
		// int chunkY = yt / Chunk.SIZE;
		// xt -= chunkX * Chunk.SIZE;
		// yt -= chunkY * Chunk.SIZE;
		
		// Chunk chunk = getLoadedChunk(new Point(chunkX, chunkY));
		// if(chunk != null)
		// return (Tile) chunk.getTile(xt, yt);
		
		// return null;
	}
	
	public Tile getClosestTile(Rectangle rect) { return getClosestTile(rect, false); }
	public Tile getClosestTile(Rectangle rect, boolean free) {
		Tile tile = getClosestTile(rect.getCenter(VectorPool.POOL.obtain()), true);
		if(free) RectPool.POOL.free(rect);
		return tile;
	}
	public Tile getClosestTile(Vector2 center) { return getClosestTile(center, false); }
	public Tile getClosestTile(Vector2 center, boolean free) {
		Tile tile = getClosestTile(center.x, center.y);
		if(free) VectorPool.POOL.free(center);
		return tile;
	}
	public Tile getClosestTile(float x, float y) {
		x = MyUtils.clamp(x, 0, getWidth()-1);
		y = MyUtils.clamp(y, 0, getHeight()-1);
		return getTile(x, y);
	}
	
	public void forAreaTiles(Point tilePos, int radius, boolean includeCenter, ValueAction<Tile> action) {
		forAreaTiles(tilePos.x, tilePos.y, radius, includeCenter, action);
	}
	public void forAreaTiles(int x, int y, int radius, boolean includeCenter, ValueAction<Tile> action) {
		for(int xo = Math.max(0, x-radius); xo <= Math.min(getWidth()-1, x+radius); xo++) {
			for(int yo = Math.max(0, y-radius); yo <= Math.min(getHeight()-1, y+radius); yo++) {
				if(xo == x && yo == y && !includeCenter)
					continue;
				action.act(tiles[xo][yo]);
			}
		}
	}
	
	public HashSet<Tile> getAreaTiles(Point tilePos, int radius, boolean includeCenter) { return getAreaTiles(tilePos.x, tilePos.y, radius, includeCenter); }
	public HashSet<Tile> getAreaTiles(int x, int y, int radius, boolean includeCenter) {
		
		HashSet<Tile> tiles = new HashSet<>();
		forAreaTiles(x, y, radius, includeCenter, tiles::add);
		
		return tiles;
	}
	
	public void forOverlappingTiles(Rectangle rect, ValueAction<Tile> action) { forOverlappingTiles(rect, false, action); }
	public void forOverlappingTiles(Rectangle rect, boolean free, ValueAction<Tile> action) {
		// Array<Point> overlappingTiles = new Array<>();
		int minX = Math.max(0, (int) rect.x);
		int minY = Math.max(0, (int) rect.y);
		int maxX = Math.min(getWidth()-1, (int) (rect.x + rect.width));
		int maxY = Math.min(getHeight()-1, (int) (rect.y + rect.height));
		
		// strange iteration order might be for rendering purposes..?
		for(int x = minX; x <= maxX; x++) {
			for (int y = maxY; y >= minY; y--) {
				action.act(tiles[x][y]);
			}
		}
		
		if(free) RectPool.POOL.free(rect);
	}
	
	public Array<Tile> getOverlappingTiles(Rectangle rect) { return getOverlappingTiles(rect, false); }
	public Array<Tile> getOverlappingTiles(Rectangle rect, boolean free) {
		Array<Tile> overlappingTiles = new Array<>();
		forOverlappingTiles(rect, free, overlappingTiles::add);
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
	
	public void forOverlappingEntities(Rectangle rect, ValueAction<Entity> action) {
		forOverlappingEntities(rect, false, null, action);
	}
	public void forOverlappingEntities(Rectangle rect, @Nullable Entity exclude, ValueAction<Entity> action) {
		forOverlappingEntities(rect, false, exclude, action);
	}
	public void forOverlappingEntities(Rectangle rect, boolean free, ValueAction<Entity> action) {
		forOverlappingEntities(rect, free, null, action);
	}
	public void forOverlappingEntities(Rectangle rect, boolean free, @Nullable Entity exclude, ValueAction<Entity> action) {
		// Array<Entity> overlapping = new Array<>(Entity.class);
		forEachEntity(entity -> {
			Rectangle bounds = entity.getBounds();
			if(bounds.overlaps(rect) && exclude != entity)
				action.act(entity);
				
			RectPool.POOL.free(bounds);
		});
		
		if(free) RectPool.POOL.free(rect);
	}
	
	public void forOverlappingObjects(Rectangle area, ValueAction<WorldObject> action) { forOverlappingObjects(area, false, action); }
	public void forOverlappingObjects(Rectangle area, boolean free, ValueAction<WorldObject> action) {
		// Array<WorldObject> objects = new Array<>(WorldObject.class);
		forOverlappingTiles(area, action::act);
		forOverlappingEntities(area, action::act);
		if(free) RectPool.POOL.free(area);
		// return objects;
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
	public Player getClosestPlayer(final Vector2 pos) { return getClosestPlayer(pos, false); }
	public Player getClosestPlayer(final Vector2 pos, boolean free) {
		final ValueWrapper<Float> minDist = new ValueWrapper<>(-1f);
		final ValueWrapper<Player> closest = new ValueWrapper<>(null);
		forEachEntity(e -> {
			if(e instanceof Player) {
				Player p = (Player) e;
				float dist = p.getDistanceTo(pos);
				if(minDist.value < 0 || minDist.value > dist) {
					minDist.value = dist;
					closest.value = p;
				}
			}
		});
		
		if(free) VectorPool.POOL.free(pos);
		
		return closest.value;
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
	public int hashCode() { return levelId.hashCode(); }
	
	@Override
	public String toString() { return getClass().getSimpleName()+"(levelId="+ levelId +')'; }
	
	@Override
	public LevelTag getTag() { return new LevelTag(levelId); }
	
	public static class LevelTag implements Tag<Level> {
		
		private final LevelId levelId;
		
		public LevelTag(LevelId levelId) { this.levelId = levelId; }
		
		@Override
		public Level getObject(WorldManager world) {
			return world.getLevel(levelId);
		}
	}
}
