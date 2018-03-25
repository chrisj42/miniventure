package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import miniventure.game.util.MyUtils;
import miniventure.game.util.SynchronizedAccessor;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Level {
	
	private final int depth, width, height;
	
	@NotNull private final WorldManager world;
	protected final SynchronizedAccessor<Map<Point, Chunk>> loadedChunks = new SynchronizedAccessor<>(Collections.synchronizedMap(new HashMap<>()));
	protected int tileCount;
	
	// this is to track when entities go in and out of chunks
	protected final HashMap<Entity, Point> entityChunks = new HashMap<>();
	
	/** @noinspection FieldCanBeLocal*/
	private int entityCap = 8; // per chunk
	
	//protected Level(@NotNull WorldManager world) { this(world, 0, 0, 0); }
	Level(@NotNull WorldManager world, int depth, int width, int height) {
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
	public int getEntityCap() { return entityCap*getLoadedChunkCount(); }
	public int getEntityCount() { return world.getEntityCount(this); }
	
	protected int getLoadedChunkCount() { return loadedChunks.get(Map::size); }
	protected Chunk getLoadedChunk(Point p) { return loadedChunks.get(chunks -> chunks.get(p)); }
	protected Chunk[] getLoadedChunkArray() { return loadedChunks.get(chunks -> chunks.values().toArray(new Chunk[chunks.size()])); }
	public boolean isChunkLoaded(Point p) { return loadedChunks.get(chunks -> chunks.containsKey(p)); }
	protected void putLoadedChunk(Point p, Chunk c) { loadedChunks.access(chunks -> chunks.put(p, c)); }
	
	public synchronized void entityMoved(Entity entity) {
		Point prevChunk = entityChunks.get(entity);
		Point curChunk = entity.getLevel() != this ? null : Chunk.getCoords(entity.getCenter());
		
		if(MyUtils.nullablesAreEqual(prevChunk, curChunk)) return;
		//System.out.println(world+" entity "+entity+" changed chunk, from "+prevChunk+" to "+curChunk);
		
		if(curChunk == null)
			entityChunks.remove(entity);
		else
			entityChunks.put(entity, curChunk);
		
		if(!world.isKeepAlive(entity)) return;
		
		System.out.println("keep alive changed chunk on "+world);
		
		pruneLoadedChunks();
		
		// load any new chunks surrounding the given entity
		for (Point p : getAreaChunkCoords(entity.getCenter(), 1, false, true)) {
			System.out.println("loading chunk on "+world+" at "+p);
			loadChunk(p);
		}
	}
	
	void pruneLoadedChunks() {
		// check for any chunks that no longer need to be loaded
		Array<Point> chunkCoords = new Array<>(loadedChunks.<Point[]>get(chunks -> chunks.keySet().toArray(new Point[chunks.size()])));
		for(WorldObject obj: world.getKeepAlives(this)) // remove loaded chunks in radius
			chunkCoords.removeAll(getAreaChunkCoords(obj.getCenter(), 2, true, false), false);
		
		// chunkCoords now contains all chunks which have no nearby keepAlive object, so they should be unloaded.
		for(Point chunkCoord: chunkCoords) {
			Chunk chunk = getLoadedChunk(chunkCoord);
			if(chunk == null) continue; // shouldn't happen, but just in case
			
			// decrease tile count by number of tiles in chunk
			tileCount -= chunk.width * chunk.height;
			// get all entities in the chunk, and remove them from the game (later they'll be saved instead)
			Array<Entity> entities = getOverlappingEntities(new Rectangle(chunkCoord.x * Chunk.SIZE, chunkCoord.y * Chunk.SIZE, chunk.width, chunk.height));
			for(Entity e: entities)
				e.remove();
			
			// I don't think I have to worry about the tiles...
			// now, remove the chunk from the set of loaded chunks
			unloadChunk(chunkCoord);
		}
	}
	
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
		for(Entity e: entities)
			e.update(delta);
	}
	
	public Array<Vector3> renderLighting(Rectangle renderSpace) {
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(renderSpace));
		objects.addAll(getOverlappingEntities(renderSpace));
		
		Array<Vector3> lighting = new Array<>();
		
		for(WorldObject obj: objects) {
			float lightR = obj.getLightRadius();
			if(lightR > 0)
				lighting.add(new Vector3(obj.getCenter(), lightR));
		}
		
		return lighting;
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
			for (int y = minY; y <= maxY; y++) {
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
		Array<Entity> overlapping = new Array<>();
		world.actOnEntitySet(this, set -> {
			for(Entity entity: set)
				if(entity.getBounds().overlaps(rect))
					overlapping.add(entity);
		});
		
		overlapping.removeAll(new Array<>(exclude), true); // use ==, not .equals()
		
		return overlapping;
	}
	
	public Array<Tile> getAreaTiles(int x, int y, int radius, boolean includeCenter) {
		Array<Tile> tiles = new Array<>();
		for(int xo = Math.max(0, x-radius); xo <= Math.min(getWidth()-1, x+radius); xo++) {
			for(int yo = Math.max(0, y-radius); yo <= Math.min(getHeight()-1, y+radius); yo++) {
				Tile t = getTile(xo, yo);
				if(t != null)
					tiles.add(t);
			}
		}
		
		if(!includeCenter)
			tiles.removeValue(getTile(x, y), true);
		
		return tiles;
	}
	
	// chunkRadius is in chunks.
	public Array<Point> getAreaChunkCoords(Vector2 tilePos, int chunkRadius, boolean loaded, boolean unloaded) {
		return getAreaChunkCoords(tilePos.x, tilePos.y, chunkRadius, loaded, unloaded);
	}
	public Array<Point> getAreaChunkCoords(float x, float y, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunkCoords(Chunk.getCoord(x), Chunk.getCoord(y), radius, loaded, unloaded);
	}
	public Array<Point> getAreaChunkCoords(int chunkX, int chunkY, int radius, boolean loaded, boolean unloaded) {
		Array<Point> chunkCoords = new Array<>();
		for(int x = chunkX - radius; x <= chunkX + radius; x++) {
			for (int y = chunkY - radius; y <= chunkY + radius; y++) {
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
	
	abstract Tile createTile(int x, int y, TileType[] types, String[] data);
	
	abstract void loadChunk(Point chunkCoord);
	abstract void unloadChunk(Point chunkCoord);
	
	@Override
	public boolean equals(Object other) { return other instanceof Level && ((Level)other).depth == depth; }
	@Override public int hashCode() { return depth; }
	
	@Override
	public String toString() { return getClass().getSimpleName()+"(depth="+depth+")"; }
}
