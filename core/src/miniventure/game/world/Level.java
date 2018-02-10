package miniventure.game.world;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ItemEntity;
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
	
	private static final float percentTilesUpdatedPerSecond = 2f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	private final LevelGenerator levelGenerator;
	private final HashMap<Point, Chunk> loadedChunks = new HashMap<>();
	private int tileCount;
	
	private final HashSet<Entity> entities = new HashSet<>();
	
	private int entityCap = 50;
	
	public Level(int depth, LevelGenerator levelGenerator) {
		this.levelGenerator = levelGenerator;
		
		/*
			At any given time, I will load a chunk, and all the chunks in a 2 chunk radius.
			
			At the start, no chunks are loaded. There is a special set of WorldObjects around which the level will always keep the tiles loaded.
			When an object is added to that set, the 9 chunks around it are loaded.
				- If it moves to an adjacent chunk, then the next 3 chunks in that direction are loaded; but the original 9 remain loaded. (though perhaps the farthest 3 don't get updated?)
				- any chunks more than 2 chunks away from an object in the set will be unloaded and saved to file.
		 */
	}
	
	public int getWidth() { return levelGenerator.worldWidth; }
	public int getHeight() { return levelGenerator.worldHeight; }
	public int getEntityCap() { return entityCap; }
	
	public int getEntityCount() { return entities.size(); }
	
	public void entityMoved(Entity entity) {
		if(!GameCore.getWorld().isKeepAlive(entity)) return;
		
		if(entity.getLevel() == this) {
			// load all surrounding chunks
			for (Point p : getAreaChunks(entity.getCenter(), 1, false, true)) {
				Chunk newChunk = new Chunk(p.x, p.y, this, levelGenerator.generateChunk(p.x, p.y));
				tileCount += newChunk.width * newChunk.height;
				loadedChunks.put(p, newChunk);
			}
		}
		
		// now check for any chunks that no longer need to be loaded
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
	
	public void addEntity(Entity e, Vector2 pos) { addEntity(e, pos.x, pos.y); }
	public void addEntity(Entity e, float x, float y) {
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
	
	public void update(float delta) {
		int tilesToUpdate = (int) (percentTilesUpdatedPerSecond * tileCount * delta);
		
		Object[] chunks = loadedChunks.values().toArray();
		for(int i = 0; i < tilesToUpdate; i++) {
			Chunk chunk = (Chunk) chunks[MathUtils.random(chunks.length-1)];
			int x = MathUtils.random(chunk.width-1);
			int y = MathUtils.random(chunk.height-1);
			Tile t = chunk.getTile(x, y);
			if(t != null) t.update(delta);
		}
		
		// update entities
		Entity[] entities = this.entities.toArray(new Entity[this.entities.size()]);
		for(Entity e: entities)
			e.update(delta);
		
		if(this.entities.size() < entityCap && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)].makeMob());
	}
	
	private void spawnMob(Mob mob, Tile[] tiles) {
		if(tiles.length == 0) throw new IllegalArgumentException("Tile array for spawning mobs must have at least one tile in it. (tried to spawn mob "+mob+")");
		
		Tile spawnTile;
		if(tiles.length == 1)
			spawnTile = tiles[0];
		else {
			do spawnTile = tiles[MathUtils.random(tiles.length - 1)];
			while (spawnTile == null || !mob.maySpawn(spawnTile.getType()));
		}
		
		mob.moveTo(spawnTile);
		addEntity(mob);
	}
	
	public void spawnMob(Mob mob) {
		// only spawns on loaded chunks
		Array<Tile> tiles = new Array<>(false, loadedChunks.size() * Chunk.SIZE * Chunk.SIZE, Tile.class);
		for(Chunk chunk: loadedChunks.values())
			for(Tile[] chunkTiles: chunk.getTiles())
				tiles.addAll(chunkTiles);
		
		tiles.shrink();
		
		spawnMob(mob, tiles.toArray(Tile.class));
	}
	
	public void spawnMob(Mob mob, Rectangle spawnArea) {
		// only spawns within the given area
		spawnArea.fitInside(new Rectangle(0, 0, getWidth(), getHeight()));
		// if the mob is a keepAlive mob, then unloaded tiles are considered; otherwise, they are not.
		if(!GameCore.getWorld().isKeepAlive(mob))
			spawnMob(mob, getOverlappingTiles(spawnArea).toArray(Tile.class));
		else {
			int x, y;
			TileType type;
			do {
				x = MathUtils.random((int)spawnArea.x, (int) (spawnArea.x+spawnArea.width));
				y = MathUtils.random((int)spawnArea.y, (int) (spawnArea.y+spawnArea.height));
				Tile tile = getTile(x, y);
				if(tile == null)
					type = levelGenerator.generateTile(x, y);
				else
					type = tile.getType();
			} while(!mob.maySpawn(type));
			
			spawnMob(mob, new Tile[] {new Tile(this, x, y, type)});
		}
	}
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(renderSpace)); // tiles first
		
		//int tileCount = objects.size;
		
		Array<Entity> entities = getOverlappingEntities(renderSpace); // entities second
		entities.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		objects.addAll(entities);
		
		//objects.shrink();
		//System.out.println("rendering "+tileCount+" tiles and "+(objects.size-tileCount)+" entities...");
		
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
	
	public void dropItem(@NotNull Item item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		ItemEntity ie = new ItemEntity(item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		Tile closest = getTile(dropPos.x, dropPos.y);
		
		Rectangle itemBounds = new Rectangle(dropPos.x, dropPos.y, item.getTexture().getRegionWidth(), item.getTexture().getRegionHeight());
		
		if(closest == null) {
			System.err.println("ERROR dropping item, closest tile is null");
			return;
		}
		
		if(!closest.isPermeableBy(ie)) {
			// we need to look around for a tile that the item *can* be placed on.
			Array<Tile> adjacent = closest.getAdjacentTiles(true);
			Tile.sortByDistance(adjacent, targetPos == null ? dropPos : targetPos);
			for(Tile adj: adjacent) {
				if(adj.isPermeableBy(ie)) {
					closest = adj;
					break;
				}
			}
		}
		
		// make sure the item will be fully inside the "closest" tile when dropped.
		MyUtils.moveRectInside(itemBounds, closest.getBounds(), 1);
		
		dropPos.x = itemBounds.x;
		dropPos.y = itemBounds.y;
		
		Vector2 dropDir;
		if(targetPos == null)
			dropDir = new Vector2().setToRandomDirection();
		else
			dropDir = targetPos.sub(dropPos);
		
		ie = new ItemEntity(item, dropDir);
		
		ie.moveTo(this, dropPos);
		addEntity(ie);
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
		
		//if(load)
		//	return new Tile(levelGenerator.generateTile((int)x, (int)y));
		
		return null;
	}
	
	public Array<Tile> getOverlappingTiles(Rectangle rect) {
		Array<Tile> overlappingTiles = new Array<>();
		int minX = Math.max(0, (int) rect.x);
		int minY = Math.max(0, (int) rect.y);
		int maxX = Math.min(getWidth(), (int) (rect.x + rect.width));
		int maxY = Math.min(getHeight(), (int) (rect.y + rect.height));
		
		for(int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				Tile tile = getTile(x, y);
				if(tile != null)
					overlappingTiles.add(tile);
			}
		}
		
		return overlappingTiles;
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
	
	private Array<Point> getAreaChunks(Vector2 tilePos, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunks(tilePos.x, tilePos.y, radius, loaded, unloaded);
	}
	private Array<Point> getAreaChunks(float x, float y, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunks(((int)x) / Chunk.SIZE, ((int)y) / Chunk.SIZE, radius, loaded, unloaded);
	}
	private Array<Point> getAreaChunks(int chunkX, int chunkY, int radius, boolean loaded, boolean unloaded) {
		Array<Point> chunkCoords = new Array<>();
		for(int x = chunkX - radius; x <= chunkX + radius; x++) {
			for (int y = chunkY - radius; y <= chunkY + radius; y++) {
				if (levelGenerator.chunkExists(x, y)) {
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
	
	
	
	
	private static final String[] levelNames = {"Surface"};
	private static final int minDepth = 0;
	
	private static Level[] levels = new Level[0];
	private static final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	public static void clearLevels() {
		entityLevels.clear();
		for(Level level: levels)
			level.entities.clear();
		levels = new Level[0];
	}
	
	public static void resetLevels(LoadingScreen display, LevelGenerator levelGenerator) {
		clearLevels();
		levels = new Level[levelNames.length];
		display.pushMessage("Loading level 0/"+levels.length+"...");
		for(int i = 0; i < levels.length; i++) {
			display.editMessage("Loading level "+(i+1)+"/"+levels.length+"...");
			levels[i] = new Level(i + minDepth, levelGenerator);
		}
		display.popMessage();
	}
	
	@Nullable
	public static Level getLevel(int depth) {
		int idx = depth-minDepth;
		return idx >= 0 && idx < levels.length ? levels[idx] : null;
	}
	
	@Nullable
	public static Level getEntityLevel(Entity entity) { return entityLevels.get(entity); }
}
