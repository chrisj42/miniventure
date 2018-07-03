package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.GameProtocol.EntityRemoval;
import miniventure.game.GameProtocol.TileUpdate;
import miniventure.game.item.Item;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.MobAi;
import miniventure.game.world.entity.mob.ServerMob;
import miniventure.game.world.entity.particle.ItemEntity;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection EqualsAndHashcode*/
public class ServerLevel extends Level {
	
	private static final float percentTilesUpdatedPerSecond = 0.02f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	private final Set<Tile> newTileUpdates = Collections.synchronizedSet(new HashSet<>());
	private final HashMap<Tile, Float> tileUpdateQueue = new HashMap<>();
	
	//private float timeCache = 0; // this is used when you should technically be updating < 1 tile in a frame.
	
	private final LevelGenerator levelGenerator;
	
	public ServerLevel(WorldManager world, int depth, LevelGenerator levelGenerator) {
		super(world, depth, levelGenerator.worldWidth, levelGenerator.worldHeight);
		this.levelGenerator = levelGenerator;
	}
	
	@Override
	public void entityMoved(Entity entity) {
		Point prevChunk = entityChunks.get(entity);
		super.entityMoved(entity);
		Point newChunk = entityChunks.get(entity);
		
		if(!MyUtils.nullablesAreEqual(prevChunk, newChunk)) {
			//	System.out.println("Server broadcasting entity "+(newChunk==null?"removal":"addition")+": "+entity);
			if(newChunk != null)
				ServerCore.getServer().broadcast(new EntityAddition(entity), this);
			else
				ServerCore.getServer().broadcast(new EntityRemoval(entity), this);
		}
	}
	
	public void onTileUpdate(ServerTile tile) {
		ServerCore.getServer().broadcast(new TileUpdate(tile), this);
		
		HashSet<Tile> tiles = getAreaTiles(tile.getLocation(), 1, true);
		
		synchronized (newTileUpdates) {
			newTileUpdates.addAll(tiles);
		}
	}
	
	public void update(Entity[] entities, float delta) {
		if(getLoadedChunkCount() == 0) return;
		
		// tiles updated
		/*float tilesPerSecond = percentTilesUpdatedPerSecond * tileCount;
		float secondsPerTile = 1 / tilesPerSecond;
		timeCache += delta;
		
		int tilesToTick = (int) (tilesPerSecond * timeCache);
		//System.out.println("server ticking "+tilesToTick+" tiles (tiles updated / sec: "+tilesPerSecond+", sec/tile: "+secondsPerTile+" time cache = "+timeCache+")");
		timeCache -= tilesToTick * secondsPerTile; // subtract time that could be "used"; not all can be used each cycle due to integer math.
		
		Chunk[] chunks = getLoadedChunkArray();
		for(int i = 0; i < tilesToTick; i++) {
			Chunk chunk = chunks[MathUtils.random(chunks.length-1)];
			int x = MathUtils.random(chunk.width-1);
			int y = MathUtils.random(chunk.height-1);
			Tile t = chunk.getTile(x, y);
			if(t != null) t.tick();
		}*/
		
		// update the tiles in the queue
		
		// store new and clear the cache first so we won't lose any updates added while updating.
		Set<Tile> tilesToUpdate;
		synchronized (newTileUpdates) {
			tilesToUpdate = new HashSet<>(newTileUpdates);
			newTileUpdates.clear();
		}
		
		// go over the old queued tiles, and decrement their update timers; for any that reach zero, remove them from the waiting list, and add them to the tiles to update.
		Iterator<Map.Entry<Tile, Float>> iter = tileUpdateQueue.entrySet().iterator();
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
			float interval = tile.update();
			if(interval > 0)
				tileUpdateQueue.put(tile, interval);
		}
		
		
		// update entities
		updateEntities(entities, delta);
		
		if(entities.length < getMobCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(new MobAi(AiType.values[MathUtils.random(AiType.values.length-1)]));
	}
	
	public void dropItems(@NotNull ItemDrop drop, @NotNull WorldObject source, @Nullable WorldObject target) {
		dropItems(drop, source.getCenter(), target == null ? null : target.getCenter());
	}
	public void dropItems(@NotNull ItemDrop drop, Vector2 dropPos, @Nullable Vector2 targetPos) {
		for(Item item: drop.getDroppedItems())
			dropItem(item, dropPos, targetPos);
	}
	
	public void dropItem(@NotNull Item item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) { dropItem(item, false, dropPos, targetPos); }
	public void dropItem(@NotNull Item item, boolean delayPickup, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		ItemEntity ie = new ItemEntity(item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		Tile closest = getTile(dropPos.x, dropPos.y);
		
		Rectangle itemBounds = ie.getBounds();
		itemBounds.setPosition(dropPos);
		
		if(closest == null) {
			System.err.println("ERROR dropping item, closest tile is null");
			return;
		}
		
		if(!closest.isPermeableBy(ie)) {
			// we need to look around for a tile that the item *can* be placed on.
			HashSet<Tile> adjacent = closest.getAdjacentTiles(true);
			Boundable.sortByDistance(new Array<>(adjacent.toArray(new Tile[adjacent.size()])), targetPos == null ? dropPos : targetPos);
			for(Tile adj: adjacent) {
				if(adj.isPermeableBy(ie)) {
					closest = adj;
					break;
				}
			}
		}
		
		// make sure the item will be fully inside the "closest" tile when dropped.
		MyUtils.moveRectInside(itemBounds, closest.getBounds(), 0.05f);
		
		dropPos.x = itemBounds.x;
		dropPos.y = itemBounds.y;
		
		Vector2 dropDir;
		if(targetPos == null)
			dropDir = new Vector2().setToRandomDirection();
		else
			dropDir = targetPos.cpy().sub(dropPos);
		
		ie = new ItemEntity(item, dropDir, delayPickup);
		
		ie.moveTo(this, dropPos);
		getWorld().setEntityLevel(ie, this);
	}
	
	
	private void spawnMob(ServerMob mob, Tile[] tiles) {
		if(tiles.length == 0) throw new IllegalArgumentException("Tile array for spawning mobs must have at least one tile in it. (tried to spawn mob "+mob+")");
		
		if(!mob.maySpawn()) return;
		
		Tile spawnTile;
		if(tiles.length == 1)
			spawnTile = tiles[0];
		else {
			do spawnTile = tiles[MathUtils.random(tiles.length - 1)];
			while (spawnTile == null || !mob.maySpawn(spawnTile.getType().getEnumType()));
		}
		
		mob.moveTo(spawnTile);
	}
	
	public void spawnMob(ServerMob mob) {
		// only spawns on loaded chunks
		Array<Tile> tiles = new Array<>(false, getLoadedChunkCount() * Chunk.SIZE * Chunk.SIZE, Tile.class);
		for(Chunk chunk: loadedChunks.get(Map::values))
			for(Tile[] chunkTiles: chunk.getTiles())
				tiles.addAll(chunkTiles);
		
		tiles.shrink();
		
		spawnMob(mob, tiles.toArray(ServerTile.class));
	}
	
	// only spawns within the given area
	public void spawnMob(ServerMob mob, Rectangle spawnArea) {
		// if the mob is a keepAlive mob, then unloaded tiles are considered; otherwise, they are not.
		if(!getWorld().isKeepAlive(mob))
			spawnMob(mob, getOverlappingTiles(spawnArea).toArray(Tile.class));
		else {
			if(!mob.maySpawn()) return; // if it can't spawn now at all, then don't try.
			int x, y;
			TileTypeEnum type;
			do {
				x = MathUtils.random((int)spawnArea.x, (int) (spawnArea.x+spawnArea.width));
				y = MathUtils.random((int)spawnArea.y, (int) (spawnArea.y+spawnArea.height));
				Tile tile = getTile(x, y);
				if(tile == null) {
					TileTypeEnum[] types = levelGenerator.generateTile(x, y);
					type = types[types.length-1];
				} else
					type = tile.getType().getEnumType();
			} while(!mob.maySpawn(type));
			
			loadChunk(Chunk.getCoords(x, y));
			Tile spawnTile = getTile(x, y);
			//noinspection ConstantConditions
			mob.moveTo(spawnTile);
		}
	}
	
	// note that asking for unloaded chunks will load them.
	public Array<Chunk> getAreaChunks(Vector2 tilePos, int radiusX, int radiusY, boolean loaded, boolean unloaded) {
		return getAreaChunks(Chunk.getCoords(tilePos), radiusX, radiusY, loaded, unloaded);
	}
	public Array<Chunk> getAreaChunks(Point chunkCoords, int radiusX, int radiusY, boolean loaded, boolean unloaded) {
		Array<Point> coords = getAreaChunkCoords(chunkCoords.x, chunkCoords.y, radiusX, radiusY, loaded, unloaded);
		Array<Chunk> chunks = new Array<>(false, coords.size, Chunk.class);
		for(Point p: coords) {
			if(!isChunkLoaded(p)) loadChunk(p);
			chunks.add(getLoadedChunk(p));
		}
		
		return chunks;
	}
	
	@Override
	protected void loadChunk(Point chunkCoord) {
		// TODO this will need to get redone when loading from file
		
		if(isChunkLoaded(chunkCoord)) return;
		
		loadChunk(new Chunk(chunkCoord.x, chunkCoord.y, this, levelGenerator.generateChunk(chunkCoord.x, chunkCoord.y), (x, y, types) -> new ServerTile(this, x, y, types)));
	}
	
	@Override
	protected void unloadChunk(Point chunkCoord) {
		// TODO this will need to get redone when saving to file
		
		Chunk chunk = getLoadedChunk(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		//System.out.println("Server unloading chunk "+chunkCoord);
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunkCoord))
				e.remove(); // removed from entityChunks within
		
		loadedChunks.access(chunks -> chunks.remove(chunkCoord));
	}
	
	@NotNull
	public Chunk getChunk(int cx, int cy) {
		Point p = new Point(cx, cy);
		loadChunk(p);
		return getLoadedChunk(p);
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerLevel && super.equals(other); }
}
