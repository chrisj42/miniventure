package miniventure.game.world.level;

import java.util.*;

import miniventure.game.GameCore;
import miniventure.game.network.GameProtocol.TileUpdate;
import miniventure.game.item.ServerItem;
import miniventure.game.server.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Boundable;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.ServerMob;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.entity.particle.ItemEntity;
import miniventure.game.world.file.LevelCache;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.Tile.TileData;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection EqualsAndHashcode*/
public class ServerLevel extends Level {
	
	//private static final float TILE_REFRESH_INTERVAL = 500; // every this many seconds, all tiles within the below radius of any keep-alive is updated.
	//private static final int TILE_REFRESH_RADIUS = 4; // the radius mentioned above.
	
	private final Set<Tile> newTileUpdates = Collections.synchronizedSet(new HashSet<>());
	private final HashMap<Tile, Float> tileUpdateQueue = new HashMap<>();
	
	// prevents level from being pruned before any keep-alives are added to it.
	private boolean preload = true;
	//private float timeCache = 0; // this is used when you should technically be updating < 1 tile in a frame.
	
	public ServerLevel(@NotNull ServerWorld world, int levelId, TileTypeEnum[][][] tiles) {
		super(world, levelId, tiles, ServerTile::new);
	}
	
	public ServerLevel(@NotNull ServerWorld world, int levelId, TileData[][] tileData) {
		super(world, levelId, tileData, ServerTile::new);
	}
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	@Override
	public int getEntityCount() { return getWorld().getEntityCount(this); }
	
	@Override
	public HashSet<ServerEntity> getEntities() { return getWorld().getEntities(this); }
	
	@Override
	public ServerTile getTile(float x, float y) { return (ServerTile) super.getTile(x, y); }
	
	public boolean isPreload() { return preload; }
	
	public void save(@NotNull LevelCache cache) {
		TileData[][] tileData = getTileData();
		LinkedList<String> entityData = new LinkedList<>();
		for(Entity e: getEntities()) {
			if(e instanceof ServerPlayer)
				continue;
			entityData.add(ServerEntity.serialize((ServerEntity)e));
		}
		cache.updateData(entityData.toArray(new String[0]), tileData);
	}
	
	/*@Override
	public void entityMoved(Entity entity) {
		Point prevChunk = entityChunks.get(entity);
		super.entityMoved(entity);
		Point newChunk = entityChunks.get(entity);
		
		if(!Objects.equals(prevChunk, newChunk)) {
			//	System.out.println("Server broadcasting entity "+(newChunk==null?"removal":"addition")+": "+entity);
			if(newChunk != null)
				getServer().broadcast(new EntityAddition(entity), this);
			else
				getServer().broadcast(new EntityRemoval(entity), this);
		}
	}*/
	
	public void onTileUpdate(ServerTile tile) {
		getServer().broadcast(new TileUpdate(tile), this);
		
		HashSet<Tile> tiles = getAreaTiles(tile.getLocation(), 1, true);
		
		newTileUpdates.addAll(tiles);
	}
	
	//private float updateAllDelta = 0;
	@Override
	public void update(float delta) {
		// if(getLoadedChunkCount() == 0) return;
		
		// update the tiles in the queue
		
		// store new and clear the cache first so we won't lose any updates added while updating.
		Set<Tile> tilesToUpdate;
		synchronized (newTileUpdates) {
			tilesToUpdate = new HashSet<>(newTileUpdates);
			newTileUpdates.clear();
		}
		
		/*if(tilesToUpdate.size() > 0) {
			System.out.println("update method; tiles to update: " + tilesToUpdate.size());
			System.out.println(tilesToUpdate);
		}*/
		
		/*updateAllDelta += delta;
		if(updateAllDelta >= TILE_REFRESH_INTERVAL) {
			updateAllDelta %= TILE_REFRESH_INTERVAL;
			for(WorldObject o: getWorld().getKeepAlives(this)) {
				if(o instanceof Tile)
					tilesToUpdate.add((Tile)o);
				else {
					Tile t = o.getClosestTile();
					if(t != null)
						tilesToUpdate.addAll(getAreaTiles(t.getLocation(), TILE_REFRESH_RADIUS, true));
				}
			}
		}*/
		
		/*
			What should happen when updating a tile:
				- tile gets changed
				- called through onTileUpdate
				- 
				
			queued tiles... are those that said they could be updated, but it hasn't been long enough.
		 */
		
		// go over the old queued tiles, and decrement their update timers; for any that reach zero, remove them from the waiting list, and add them to the tiles to update.
		Iterator<Map.Entry<Tile, Float>> iter = tileUpdateQueue.entrySet().iterator();
		// if(tileUpdateQueue.size() > 0)
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
			float interval = ((ServerTile)tile).update();
			if(interval > 0)
				tileUpdateQueue.put(tile, interval);
		}
		
		
		// update entities
		super.update(delta);
		
		if(getMobCount() < getMobCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)].makeMob(getWorld()));
	}
	
	public void entityAdded(@NotNull ServerEntity e) {
		if(preload && e instanceof Player) {
			// GameCore.debug("preload disabled.");
			preload = false;
		}
	}
	
	public void addEntity(@NotNull ServerEntity e) {
		getWorld().setEntityLevel(e, this);
	}
	
	public void dropItems(@NotNull ItemDrop drop, @NotNull WorldObject source, @Nullable WorldObject target) {
		dropItems(drop, source.getCenter(), target == null ? null : target.getCenter());
	}
	public void dropItems(@NotNull ItemDrop drop, Vector2 dropPos, @Nullable Vector2 targetPos) {
		for(ServerItem item: drop.getDroppedItems())
			dropItem(item, dropPos, targetPos);
	}
	
	public void dropItem(@NotNull ServerItem item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) { dropItem(item, false, dropPos, targetPos); }
	public void dropItem(@NotNull final ServerItem item, boolean delayPickup, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		final ItemEntity ie = new ItemEntity(getWorld(), item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		Tile closest = getClosestTile(dropPos.x, dropPos.y);
		
		Rectangle itemBounds = ie.getBounds();
		itemBounds.setPosition(dropPos);
		
		if(closest == null) {
			System.err.println("Server ERROR dropping item, closest tile is null");
			return;
		}
		
		if(!ie.canPermeate(closest)) {
			// we need to look around for a tile that the item *can* be placed on.
			HashSet<Tile> adjacent = closest.getAdjacentTiles(true);
			Boundable.sortByDistance(new Array<>(adjacent.toArray(new Tile[0])), targetPos == null ? dropPos : targetPos);
			for(Tile adj: adjacent) {
				if(ie.canPermeate(adj)) {
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
		
		getWorld().cancelIdReservation(ie);
		ItemEntity nie = new ItemEntity(getWorld(), item, dropDir, delayPickup);
		
		nie.moveTo(dropPos);
		addEntity(nie);
	}
	
	// only spawns on the given tiles
	/*private void spawnMob(ServerMob mob, ServerTile[] tiles) {
		if(tiles.length == 0) throw new IllegalArgumentException("Tile array for spawning mobs must have at least one tile in it. (tried to spawn mob "+mob+')');
		
		ServerTile spawnTile;
		if(tiles.length == 1)
			spawnTile = tiles[0];
		else {
			do spawnTile = tiles[MathUtils.random(tiles.length - 1)];
			while (spawnTile == null || !mob.maySpawn(spawnTile.getType().getTypeEnum()));
		}
		
		mob.moveTo(spawnTile);
	}*/
	
	private final ArrayList<Integer> locations = new ArrayList<>();
	{
		for(int i = 0; i < getWidth() * getHeight(); i++) {
			locations.add(i);
		}
	}
	
	public ServerTile getSpawnTile(ServerMob mob) {
		if(!mob.maySpawn()) return null;
		
		ArrayList<Integer> locations = new ArrayList<>(this.locations);
		
		while(locations.size() > 0) {
			final int idx = MathUtils.random(locations.size()-1);
			final int loc = locations.get(idx);
			final int x = loc % getWidth();
			final int y = loc / getWidth();
			Tile tile = getTile(x, y);
			if(tile == null)
				System.err.println("level contains null tile! "+x+','+y+"@level="+this);
			else if(mob.maySpawn(tile.getType().getTypeEnum()))
				return getTile(x, y);
			else
				locations.remove(idx);
		}
		// all tiles in the level were searched through; no options were found.
		
		return null;
	}
	
	public void spawnMob(ServerMob mob) {
		if(!mob.maySpawn()) return;
		ServerTile tile = getSpawnTile(mob);
		if(tile != null) {
			mob.moveTo(tile);
			addEntity(mob);
		}
		else GameCore.error("Failed to spawn mob "+mob+", no suitable spawn location.");
	}
	
	// only spawns within the given area
	/*public void spawnMob(ServerMob mob, Rectangle spawnArea) {
		// if the mob is a keepAlive mob, then unloaded tiles are considered; otherwise, they are not.
		if(!getWorld().isKeepAlive(mob))
			spawnMob(mob, getOverlappingTiles(spawnArea).toArray(Tile.class));
		else {
			if(!mob.maySpawn()) return; // if it can't spawn now at all, then don't try.
			
		}
	}*/
	
	// note that asking for unloaded chunks will load them.
	/*public Array<Chunk> getAreaChunks(Vector2 tilePos, int radiusX, int radiusY, boolean loaded, boolean unloaded) {
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
	}*/
	
	/*@Override
	protected void loadChunk(Point chunkCoord) {
		
		if(isChunkLoaded(chunkCoord)) return;
		
		loadChunk(new Chunk(chunkCoord.x, chunkCoord.y, this, levelGenerator.generateChunk(chunkCoord.x, chunkCoord.y), (x, y, types) -> new ServerTile(this, x, y, types)));
	}*/
	
	/*@Override
	protected void unloadChunk(Point chunkCoord) {
		Chunk chunk = getLoadedChunk(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		//System.out.println("Server unloading chunk "+chunkCoord);
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunkCoord))
				e.remove(); // removed from entityChunks within
		
		loadedChunks.access(chunks -> chunks.remove(chunkCoord));
	}*/
	
	/*@NotNull
	public Chunk getChunk(int cx, int cy) {
		Point p = new Point(cx, cy);
		loadChunk(p);
		return getLoadedChunk(p);
	}*/
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerLevel && super.equals(other); }
}
