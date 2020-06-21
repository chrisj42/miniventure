package miniventure.game.world.management;

import java.util.*;

import miniventure.game.item.Item;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.SpawnBehavior;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.particle.ActionParticle;
import miniventure.game.world.entity.particle.ItemEntity;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.worldgen.island.IslandType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Level {
	
	/*
		- entities made with level and position separate
		- need a kind of builder that creates entities given a level and position, and info specific to a certain entity type
	 */
	
	// multi-purpose; a general radius outside of which less effort is made to keep things loaded and updated
	public static final int ACTIVE_RADIUS = 20; // measured in tiles
	
	
	// MOB MANAGEMENT
	
	// at this distance, mobs are automatically despawned
	private static final int MOB_DESPAWN_RADIUS = 120; // measured in tiles
	
	// the time that a mob must be outside the active radius before being despawned; decreases linearly the further away the mob gets, reaching 0 at MAX_DESPAWN_RADIUS
	private static final float MAX_DESPAWN_TIME = 60; // measured in seconds
	
	// with at least this many mobs, natural spawning will not occur
	private static final int MOB_CAP = 80;
	
	// around this point, mobs will go from spawning fast to spawning slow
	private static final int MOB_SPAWN_CRITICAL_POINT = 50;
	// point - dev = end of fast spawn; point + dev = start of slow spawn. middle is transition area
	private static final int MOB_SPAWN_CRITICAL_DEVIATION = 10;
	
	private int mobCount;
	
	// TODO while I'm going to try this as a way to improve mob spawning, I should check the memory impact when I run it to ensure doing this isn't too expensive.
	private final EnumMap<TileType, TreeMap<Integer, TreeSet<Integer>>> tileTypePositionCache = new EnumMap<>(TileType.class);
	
	private final int levelId;
	private final int width;
	private final int height;
	@NotNull private final Tile[][] tiles;
	
	@NotNull private final WorldManager world;
	
	@NotNull private final Player player;
	
	// private final Map<Integer, Entity> entityIDMap = new HashMap<>(128);
	private final TreeSet<Entity> entitySet = new TreeSet<>(Comparator.comparingInt(Entity::getId));
	private int idCounter = 0;
	// since entities don't get their ID until the addEntity method returns, we can't add them to the set right away. So, we'll put them here and add them at the end of a level update.
	private final LinkedList<Entity> entityAdditionQueue = new LinkedList<>();
	
	private final Set<Tile> newTileUpdates = Collections.synchronizedSet(new HashSet<>());
	private final HashMap<Tile, Float> tileUpdateQueue = new HashMap<>();
	
	@FunctionalInterface
	private interface PlayerFetcher {
		Player getPlayer(@NotNull Level level);
	}
	
	// loading first level, no currently loaded player; player data is null for new worlds
	public Level(@NotNull WorldManager world, LevelDataSet data, @Nullable String playerData) {
		this(world, data, level -> playerData == null ?
				new Player(level.spawnPlayer()) :
				(Player) Entity.deserialize(level, playerData, data.dataVersion)
		);
	}
	
	// changing levels; pass reference to current level's player
	public Level(@NotNull Level curLevel, LevelDataSet data) {
		this(curLevel.world, data, level -> new Player(level.getSpawn(data.travelPos), curLevel.player));
	}
	
	private Level(@NotNull WorldManager world, LevelDataSet data, PlayerFetcher playerFetcher) {
		this.world = world;
		this.levelId = data.id;
		
		if(data.dataVersion == null) {
			// generate new tiles
			IslandType type = WorldManager.getIslandType(levelId);
			this.width = type.width;
			this.height = type.height;
			
			TileType[][][] types = type.generateIsland(world.getSeed(levelId), true);
			
			tiles = new Tile[width][height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					tiles[x][y] = new Tile(this, x, y, types[x][y], null);
				}
			}
		}
		else {
			this.width = data.width;
			this.height = data.height;
			// this.savedTravelPos = data.travelPos;
			
			tiles = new Tile[data.width][data.height];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					tiles[x][y] = new Tile(this, x, y, data.tileData[x][y]);
				}
			}
		}
		
		this.player = playerFetcher.getPlayer(this);
		Gdx.app.postRunnable(() -> player.updateGameScreen(world.getGameScreen()));
		
		for(String eData: data.entityData)
			Entity.deserialize(this, eData, data.dataVersion);
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getLevelId() { return levelId; }
	@NotNull public WorldManager getWorld() { return world; }
	public int getMobCap() { return MOB_CAP; }
	public int getMobCount() { return mobCount; }
	
	public int getEntityCount()  { return entitySet.size(); }
	public Set<Entity> getEntities() { return entitySet; }
	
	@NotNull
	public Player getPlayer() { return player; }
	
	public LevelDataSet save() {
		// savedTravelPos = player.getClosestTile().getLocation();
		
		String[][] tileData = new String[width][height];
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				tileData[x][y] = tiles[x][y].save();
		
		LinkedList<String> entityData = new LinkedList<>();
		for(Entity e: getEntities()) {
			if(e instanceof Player)
				continue;
			entityData.add(Entity.serialize((Entity)e));
		}
		// dataCache.updateData(entityData.toArray(new String[0]), tileData);
		
		// Point travelPos = 
		return new LevelDataSet(levelId, width, height, Version.CURRENT, tileData, entityData.toArray(new String[0]), player.getClosestTile().getLocation());
	}
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		// applyTileUpdates();
		
		renderSpace = new Rectangle(Math.max(0, renderSpace.x), Math.max(0, renderSpace.y), Math.min(getWidth()-renderSpace.x, renderSpace.width), Math.min(getHeight()-renderSpace.y, renderSpace.height));
		// pass the offset vector to all objects being rendered.
		
		Array<Tile> tiles = getOverlappingTiles(renderSpace);
		Array<Entity> entities = getOverlappingEntities(renderSpace);

		// if(GdxCore.getScreen() instanceof RespawnScreen)
		// 	entities.removeValue(GameCore.getWorld().getMainPlayer(), true);
		
		render(tiles, entities, batch, delta, posOffset);
	}
	
	public static void render(Array<Tile> tiles, Array<Entity> entities, SpriteBatch batch, float delta, Vector2 posOffset) {
		// pass the offset vector to all objects being rendered.
		
		Array<WorldObject> objects = new Array<>();
		Array<Tile> under = new Array<>(); // ground tiles
		Array<Entity> over = new Array<>();
		for(Entity e: entities) {
			if(e.isFloating() && !(e instanceof ActionParticle))
				over.add(e);
			else
				objects.add(e);
		}
		for(Tile t: tiles) {
			if(!t.getType().isWalkable()) // used to check if z offset > 0
				objects.add(t);
			else
				under.add(t);
		}
		
		// first, ground tiles
		// then, entities and surface tiles, higher y first
		// then particles
		
		// entities second
		under.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		objects.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		over.sort((e1, e2) -> Float.compare(e2.getCenter().y, e1.getCenter().y));
		//objects.addAll(entities);
		
		for(WorldObject obj: under)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: objects)
			obj.render(batch, delta, posOffset);
		for(WorldObject obj: over)
			obj.render(batch, delta, posOffset);
	}
	
	public static Array<Vector3> renderLighting(Array<WorldObject> objects) {
		Array<Vector3> lighting = new Array<>();
		
		for(WorldObject obj: objects) {
			float lightR = obj.getLightRadius();
			if(lightR > 0)
				lighting.add(new Vector3(obj.getCenter(), lightR));
		}
		
		return lighting;
	}
	
	
	public void update(float delta) {
		// if(getLoadedChunkCount() == 0) return;
		
		// update the tiles in the queue
		
		// store new and clear the cache first so we won't lose any updates added while updating.
		Set<Tile> tilesToUpdate;
		synchronized (newTileUpdates) {
			tilesToUpdate = new HashSet<>(newTileUpdates);
			newTileUpdates.clear();
		}
		
		// go over the old queued tiles, and decrement their update timers; for any that reach zero, remove them from the waiting list, and add them to the tiles to update.
		Iterator<Map.Entry<Tile, Float>> iter = tileUpdateQueue.entrySet().iterator();
		// 	System.out.println("going through tile update queue... "+tileUpdateQueue.size()+" tiles");
		while(iter.hasNext()) {
			Map.Entry<Tile, Float> entry = iter.next();
			if(tilesToUpdate.contains(entry.getKey())) {
				// has been updated prematurely; it will be re-added below
				iter.remove();
				continue;
			}
			
			float newTime = entry.getValue() - delta;
			if(newTime <= 0) {
				tilesToUpdate.add(entry.getKey());
				iter.remove();
			} else // not ready to update yet; leave in queue
				entry.setValue(newTime);
		}
		
		// go through and update all the tiles that need it; if it specifies a delay until next update, add it to the update queue.
		for(Tile tile: tilesToUpdate) {
			float interval = (tile).update();
			if(interval > 0)
				tileUpdateQueue.put(tile, interval);
		}
		
		int mobs = 0;
		for(Entity e: getEntities()) {
			e.update(delta);
			if(e instanceof Mob)
				mobs++;
		}
		this.mobCount = mobs;
		
		if(getMobCount() < getMobCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)]);
		
		// refresh addition queue
		entitySet.addAll(entityAdditionQueue);
		entityAdditionQueue.clear();
	}
	
	public void onTileUpdate(Tile tile/*, @Nullable TileType updatedType*/) {
		// getServer().broadcastLocal(this, new TileUpdate(tile, updatedType));
		
		tile.updateSprites();
		HashSet<Tile> tiles = getAreaTiles(tile.getLocation(), 1, true);
		
		newTileUpdates.addAll(tiles);
	}
	
	/*public void entityAdded(@NotNull Entity e) {
		if(preload && e instanceof Player) {
			// GameCore.debug("preload disabled.");
			preload = false;
		}
	}*/
	
	public int addEntity(@NotNull Entity e) {
		// entitySet.add(e);
		entityAdditionQueue.addLast(e);
		return idCounter++;
	}
	
	public void resetPlayer() {
		player.reset();
		EntitySpawn spawn = spawnPlayer();
		player.moveTo(spawn.getX(), spawn.getY());
		entityAdditionQueue.addLast(player);
	}
	
	public void removeEntity(Entity e) {
		entitySet.remove(e);
	}
	
	public EntitySpawn getSpawn(Point pos) { return EntitySpawn.get(this, pos.x + Tile.SIZE/2f, pos.y + Tile.SIZE/2f); }
	public EntitySpawn getSpawn(Vector2 pos) { return EntitySpawn.get(this, pos); }
	public EntitySpawn getSpawn(float x, float y) { return EntitySpawn.get(this, x, y); }
	
	public void spawnMob(AiType mobType) {
		Tile tile = mobType.getSpawnBehavior().trySpawn(this);
		if(tile != null)
			mobType.makeMob(EntitySpawn.get(this, tile.getCenter()));
		else
			MyUtils.error("Failed to spawn mob "+mobType+", no suitable spawn location.");
	}
	
	private EntitySpawn spawnPlayer() {
		Tile spawnTile = null;
		for(int i = 0; i < 100; i++) {
			final int x = MathUtils.random(width);
			final int y = MathUtils.random(height);
			Tile tile = getTile(x, y);
			if(SpawnBehavior.DEFAULT.maySpawn(tile.getType())) {
				spawnTile = tile;
				break;
			}
		}
		
		if(spawnTile == null) {
			// force a valid spawn tile; FIXME TODO I'd much rather do this another way
			spawnTile = tiles[width/2][height/4];
			spawnTile.forcePermeable();
		}
		
		return getSpawn(spawnTile.getLocation());
	}
	
	public void dropItems(@NotNull ItemDrop drop, @NotNull WorldObject source, @Nullable WorldObject target) {
		dropItems(drop, source.getCenter(), target == null ? null : target.getCenter());
	}
	public void dropItems(@NotNull ItemDrop drop, Vector2 dropPos, @Nullable Vector2 targetPos) {
		for(Item item: drop.getDroppedItems())
			dropItem(item, dropPos, targetPos);
	}
	
	public void dropItem(@NotNull Item item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) { dropItem(item, false, dropPos, targetPos); }
	public void dropItem(@NotNull final Item item, boolean delayPickup, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		// final ItemEntity ie = new ItemEntity(getWorld(), item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		Tile closest = getClosestTile(dropPos);
		if(!closest.isPermeable()) {
			// we need to look around for a tile that the item *can* be placed on.
			HashSet<Tile> adjacent = closest.getAdjacentTiles(true);
			WorldObject.sortByDistance(new Array<>(adjacent.toArray(new Tile[0])), targetPos == null ? dropPos : targetPos);
			for(Tile adj: adjacent) {
				if(adj.isPermeable()) {
					closest = adj;
					break;
				}
			}
		}
		
		// make sure the item will be fully inside the "closest" tile when dropped.
		TextureHolder itemTexture = item.getTexture();
		Rectangle itemBounds = new Rectangle(0, 0, itemTexture.width, itemTexture.height);
		MyUtils.moveRectInside(itemBounds, closest.getBounds(), 0.05f);
		
		dropPos.x = itemBounds.x;
		dropPos.y = itemBounds.y;
		
		Vector2 dropDir;
		if(targetPos == null)
			dropDir = new Vector2().setToRandomDirection();
		else
			dropDir = targetPos.cpy().sub(dropPos);
		
		// getWorld().cancelIdReservation(ie);
		new ItemEntity(getSpawn(dropPos), item, dropDir, delayPickup);
	}
	
	
	public Tile getTile(Rectangle rect) { return getTile(rect.getCenter(new Vector2())); }
	public Tile getTile(Vector2 pos) { return getTile(pos.x, pos.y); }
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
	
	// fixme far too inefficient
	/*public ArrayList<Tile> getMatchingTiles(TileType type) {
		ArrayList<Tile> matches = new ArrayList<>();
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(tiles[x][y].getType() == type)
					matches.add(tiles[x][y]);
			}
		}
		
		return matches;
	}*/
	
	/*@Nullable
	public Player getClosestPlayer(final Vector2 pos) {
		Array<Player> players = new Array<>();
		for(Entity e: getEntities())
			if(e instanceof Player)
				players.add((Player)e);
		
		if(players.size == 0) return null;
		
		players.sort((p1, p2) -> Float.compare(p1.getCenter().dst(pos), p2.getCenter().dst(pos)));
		
		return players.get(0);
	}*/
	
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
	
	/*@Override
	public LevelTag getTag() { return new LevelTag(levelId); }
	
	public static class LevelTag implements Tag<Level> {
		
		private final int levelId;
		
		public LevelTag(int levelId) { this.levelId = levelId; }
		
		@Override
		public Level getObject(WorldManager world) {
			return world.getLevel(levelId);
		}
	}*/
}
