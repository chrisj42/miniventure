package miniventure.game.world;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ItemEntity;
import miniventure.game.world.entity.Particle;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Level {
	
	private final int depth, width, height;
	
	final HashMap<Point, Chunk> loadedChunks = new HashMap<>();
	int tileCount;
	
	final HashSet<Entity> entities = new HashSet<>();
	
	/** @noinspection FieldCanBeLocal*/
	private int entityCap = 8; // per chunk
	
	Level() { this(0, 0, 0); }
	public Level(int depth, int width, int height) {
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
	public int getEntityCap() { return entityCap*loadedChunks.size(); }
	public int getEntityCount() { return entities.size(); }
	
	public void entityMoved(Entity entity) {
		if(!GameCore.getWorld().isKeepAlive(entity)) {
			// check if they've gone (mostly?) out of bounds, if so, remove them
			Vector2 pos = entity.getCenter();
			if(!loadedChunks.containsKey(new Point(Chunk.getCoord(pos.x), Chunk.getCoord(pos.y))))
				entity.remove();
			return;
		}
		
		// check for any chunks that no longer need to be loaded
		Array<Point> chunkCoords = new Array<>(loadedChunks.keySet().toArray(new Point[loadedChunks.size()]));
		for(WorldObject obj: GameCore.getWorld().getKeepAlives(this)) // remove loaded chunks in radius
			chunkCoords.removeAll(getAreaChunks(obj.getCenter(), 2, true, false), false);
		
		// chunkCoords now contains all chunks which have no nearby keepAlive object, so they should be unloaded.
		for(Point chunkCoord: chunkCoords) {
			Chunk chunk = loadedChunks.get(chunkCoord);
			if(chunk == null) continue; // shouldn't happen, but just in case
			
			// decrease tile count by number of tiles in chunk
			tileCount -= chunk.width * chunk.height;
			// get all entities in the chunk, and remove them from the game (later they'll be saved instead)
			Array<Entity> entities = getOverlappingEntities(new Rectangle(chunkCoord.x * Chunk.SIZE, chunkCoord.y * Chunk.SIZE, chunk.width, chunk.height));
			for(Entity e: entities)
				e.remove();
			
			// I don't think I have to worry about the tiles...
			// now, remove the chunk from the set of loaded chunks
			loadedChunks.remove(chunkCoord);
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
		addEntity(e);
	}
	public void addEntity(Entity e) {
		entities.add(e);
		Level oldLevel = entityLevels.put(e, this); // replaces the level for the entity
		if (oldLevel != null && oldLevel != this)
			oldLevel.removeEntity(e); // remove it from the other level's entity set.
		
		entityMoved(e);
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e);
		if (entityLevels.get(e) == this)
			entityLevels.remove(e);
		
		entityMoved(e);
	}
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		renderSpace = new Rectangle(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		//System.out.println("render space: " + renderSpace);
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(renderSpace)); // tiles first
		
		Array<Entity> entities = getOverlappingEntities(renderSpace); // entities second
		entities.sort((e1, e2) -> {
			if(e1 instanceof Particle && !(e2 instanceof Particle))
				return 1;
			if(!(e1 instanceof Particle) && e2 instanceof Particle)
				return -1;
			return Float.compare(e2.getCenter().y, e1.getCenter().y);
		});
		objects.addAll(entities);
		
		for(WorldObject obj: objects)
			obj.render(batch, delta, posOffset);
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
	public Tile getTile(float x, float y) {
		if(x < 0 || y < 0 || x > getWidth() || y > getHeight())
			return null;
		
		int xt = (int) x;
		int yt = (int) y;
		int chunkX = xt / Chunk.SIZE;
		int chunkY = yt / Chunk.SIZE;
		xt -= chunkX * Chunk.SIZE;
		yt -= chunkY * Chunk.SIZE;
		
		Chunk chunk = loadedChunks.get(new Point(chunkX, chunkY));
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
	
	Array<Point> getOverlappingChunks(Rectangle rect) {
		Array<Point> overlappingChunks = new Array<>();
		Array<Point> points = getOverlappingTileCoords(rect);
		
		for(Point p: points) {
			Point chunk = new Point(Chunk.getCoord(p.x), Chunk.getCoord(p.y));
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
		for(Entity entity: entities)
			if(entity.getBounds().overlaps(rect))
				overlapping.add(entity);
		
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
	
	Array<Point> getAreaChunks(Vector2 tilePos, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunks(tilePos.x, tilePos.y, radius, loaded, unloaded);
	}
	Array<Point> getAreaChunks(float x, float y, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunks(Chunk.getCoord(x), Chunk.getCoord(y), radius, loaded, unloaded);
	}
	Array<Point> getAreaChunks(int chunkX, int chunkY, int radius, boolean loaded, boolean unloaded) {
		Array<Point> chunkCoords = new Array<>();
		for(int x = chunkX - radius; x <= chunkX + radius; x++) {
			for (int y = chunkY - radius; y <= chunkY + radius; y++) {
				if (chunkExists(x, y)) {
					Point p = new Point(x, y);
					boolean isLoaded = loadedChunks.containsKey(p);
					if(loaded && isLoaded || unloaded && !isLoaded)
						chunkCoords.add(new Point(x, y));
				}
			}
		}
		
		return chunkCoords;
	}
	
	@Nullable
	public Tile getClosestTile(Rectangle rect) {
		Vector2 center = rect.getCenter(new Vector2());
		return getTile(center.x, center.y);
	}
	
	@Nullable
	public Player getClosestPlayer(final Vector2 pos) {
		Array<Player> players = new Array<>();
		for(Entity e: entities)
			if(e instanceof Player)
				players.add((Player)e);
		
		if(players.size == 0) return null;
		
		players.sort((p1, p2) -> Float.compare(p1.getCenter().dst(pos), p2.getCenter().dst(pos)));
		
		return players.get(0);
	}
	
	public boolean chunkExists(int x, int y) {
		if(x < 0 || y < 0) return false;
		
		if(x * Chunk.SIZE >= getWidth() || y * Chunk.SIZE >= getHeight())
			return false;
		
		return true;
	}
	
	
	@Override
	public boolean equals(Object other) { return other instanceof Level && ((Level)other).depth == depth; }
	@Override public int hashCode() { return depth; }
	
	
	
	
	
	
	static final String[] levelNames = {"Surface"};
	static final int minDepth = 0;
	
	static Level[] levels = new Level[0];
	private static final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	public static void clearLevels() {
		entityLevels.clear();
		for(Level level: levels)
			level.entities.clear();
		levels = new Level[0];
	}
	
	public static void resetLevels(Level... levels) {
		clearLevels();
		Level.levels = levels;
	}
	
	@Nullable
	public static Level getLevel(int depth) {
		int idx = depth-minDepth;
		return idx >= 0 && idx < levels.length ? levels[idx] : null;
	}
	
	@Nullable
	public static Level getEntityLevel(Entity entity) { return entityLevels.get(entity); }
}
