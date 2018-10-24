package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import miniventure.game.util.SynchronizedAccessor;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Level implements Taggable<Level> {
	
	public static final int X_LOAD_RADIUS = 4, Y_LOAD_RADIUS = 2;
	
	private final int depth, width, height;
	
	@NotNull private final WorldManager world;
	final SynchronizedAccessor<Map<Point, Chunk>> loadedChunks = new SynchronizedAccessor<>(Collections.synchronizedMap(new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2)));
	//private int tileCount;
	private int mobCount;
	
	// this is to track when entities go in and out of chunks
	final HashMap<Entity, Point> entityChunks = new HashMap<>(X_LOAD_RADIUS*2*Y_LOAD_RADIUS*2);
	
	/** @noinspection FieldCanBeLocal*/
	private int mobCap = 3; // per chunk
	
	//protected Level(@NotNull WorldManager world) { this(world, 0, 0, 0); }
	protected Level(@NotNull WorldManager world, int depth, int width, int height) {
		this.world = world;
		this.depth = depth;
		this.width = width;
		this.height = height;
		
		/*
			At any given time, I will load a chunk, and all the chunks in a 2 chunk radius.
			
			At the start, no chunks are loaded. There is a special set of WorldObjects around which the level will always keep the tiles loaded.
			When an object is added to that set, the 9 chunks around it are loaded.
				- If it moves to an adjacent chunk, then the next 3 chunks in that direction are loaded; but the original 9 remain loaded. (though perhaps the farthest 3 don't get updated?)
				- any chunks more than 2 chunks away from an object in the set will be unloaded and saved to file.
		 */
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getDepth() { return depth; }
	@NotNull public WorldManager getWorld() { return world; }
	public int getMobCap() { return mobCap *getLoadedChunkCount(); }
	public int getMobCount() { return mobCount; }
	public int getEntityCount() { return world.getEntityCount(this); }
	
	public Entity[] getEntities() { return world.getEntities(this); }
	
	protected int getLoadedChunkCount() { return loadedChunks.get(Map::size); }
	protected Chunk getLoadedChunk(Point p) { return loadedChunks.get(chunks -> chunks.get(p)); }
	protected Chunk[] getLoadedChunkArray() { return loadedChunks.get(chunks -> chunks.values().toArray(new Chunk[chunks.size()])); }
	public boolean isChunkLoaded(Point p) { return loadedChunks.get(chunks -> chunks.containsKey(p)); }
	protected void putLoadedChunk(Point p, Chunk c) { loadedChunks.access(chunks -> chunks.put(p, c)); }
	
	public synchronized void entityMoved(Entity entity) {
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
	}
	
	void pruneLoadedChunks() {
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
	}
	
	public abstract void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset);
	
	public void addEntity(Entity e, Vector2 pos, boolean center) { addEntity(e, pos.x, pos.y, center); }
	public void addEntity(Entity e, float x, float y, boolean center) {
		if(center) {
			Vector2 size = e.getSize();
			x -= size.x/2;
			y -= size.y/2;
		}
		e.moveTo(this, x, y);
	}
	
	public void updateEntities(Entity[] entities, float delta) {
		int mobs = 0;
		for(Entity e: entities) {
			e.update(delta);
			if(e.isMob())
				mobs++;
		}
		this.mobCount = mobs;
	}
	
	@Nullable
	public Tile getTile(Vector2 pos) { return getTile(pos.x, pos.y); }
	@Nullable
	public Tile getTile(float x, float y) {
		if(x < 0 || y < 0 || x > getWidth() || y > getHeight())
			return null;
		
		int xt = (int) x;
		int yt = (int) y;
		int chunkX = xt / Chunk.SIZE;
		int chunkY = yt / Chunk.SIZE;
		xt -= chunkX * Chunk.SIZE;
		yt -= chunkY * Chunk.SIZE;
		
		Chunk chunk = getLoadedChunk(new Point(chunkX, chunkY));
		if(chunk != null)
			return chunk.getTile(xt, yt);
		
		return null;
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
	
	protected Array<Point> getOverlappingChunks(Rectangle rect) {
		Array<Point> overlappingChunks = new Array<>();
		Array<Point> points = getOverlappingTileCoords(rect);
		
		for(Point p: points) {
			Point chunk = Chunk.getCoords(p);
			if(!overlappingChunks.contains(chunk, false))
				overlappingChunks.add(chunk);
		}
		
		return overlappingChunks;
	}
	
	public Array<Entity> getOverlappingEntities(Rectangle rect) {
		return getOverlappingEntities(rect, (Entity)null);
	}
	public Array<Entity> getOverlappingEntities(Rectangle rect, Entity... exclude) {
		Array<Entity> overlapping = new Array<>(Entity.class);
		world.actOnEntitySet(this, set -> {
			for(Entity entity: set)
				if(entity.getBounds().overlaps(rect))
					overlapping.add(entity);
		});
		
		overlapping.removeAll(new Array<>(exclude), true); // use ==, not .equals()
		
		return overlapping;
	}
	
	public Array<WorldObject> getOverlappingObjects(Rectangle area) {
		Array<WorldObject> objects = new Array<>(WorldObject.class);
		objects.addAll(getOverlappingTiles(area));
		objects.addAll(getOverlappingEntities(area));
		return objects;
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
	
	// chunkRadius is in chunks.
	public Array<Point> getAreaChunkCoords(Vector2 tilePos, int chunkRadiusX, int chunkRadiusY, boolean loaded, boolean unloaded) {
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
	}
	
	@Nullable
	public Tile getClosestTile(Rectangle rect) { return getClosestTile(rect.getCenter(new Vector2())); }
	public Tile getClosestTile(Vector2 center) { return getTile(center.x, center.y); }
	
	@Nullable
	public Player getClosestPlayer(final Vector2 pos) {
		Array<Player> players = new Array<>();
		world.actOnEntitySet(this, set -> {
			for(Entity e: set)
				if(e instanceof Player)
					players.add((Player)e);
		});
		
		if(players.size == 0) return null;
		
		players.sort((p1, p2) -> Float.compare(p1.getCenter().dst(pos), p2.getCenter().dst(pos)));
		
		return players.get(0);
	}
	
	public boolean chunkExists(int cx, int cy) { return tileExists(cx * Chunk.SIZE, cy * Chunk.SIZE); }
	public boolean tileExists(int tx, int ty) {
		if(tx < 0 || ty < 0) return false;
		
		if(tx >= getWidth() || ty >= getHeight())
			return false;
		
		return true;
	}
	
	protected abstract void loadChunk(Point chunkCoord);
	protected abstract void unloadChunk(Point chunkCoord);
	
	public void loadChunk(Chunk newChunk) {
		//tileCount += newChunk.width * newChunk.height;
		putLoadedChunk(new Point(newChunk.chunkX, newChunk.chunkY), newChunk);
		
		// queue all contained tiles for update
		Tile[][] tiles = newChunk.getTiles();
		for(int i = 0; i < tiles.length; i++) {
			for(int j = 0; j < tiles[i].length; j++) {
				Tile t = tiles[i][j];
				t.updateSprites();
				// update the tiles in adjacent chunks
				int oi = i == 0 ? -1 : i == tiles.length-1 ? 1 : 0;
				int oj = j == 0 ? -1 : j == tiles[i].length-1 ? 1 : 0;
				if(oi != 0) tryUpdate(t, oi, 0); // left/right side
				if(oj != 0) tryUpdate(t, 0, oj); // above/below
				if(oi != 0 && oj != 0) tryUpdate(t, oi, oj); // corner
			}
		}
	}
	private void tryUpdate(Tile ref, int ox, int oy) {
		Point p = ref.getLocation();
		Tile tile = getTile(p.x+ox, p.y+oy);
		if(tile != null) tile.updateSprites();
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof Level && ((Level)other).depth == depth; }
	@Override public int hashCode() { return depth; }
	
	@Override
	public String toString() { return getClass().getSimpleName()+"(depth="+depth+")"; }
	
	@Override
	public LevelTag getTag() { return new LevelTag(depth); }
	
	public static class LevelTag implements Tag<Level> {
		
		private final int depth;
		
		public LevelTag(int depth) { this.depth = depth; }
		
		@Override
		public Level getObject(WorldManager world) {
			return world.getLevel(depth);
		}
	}
}
