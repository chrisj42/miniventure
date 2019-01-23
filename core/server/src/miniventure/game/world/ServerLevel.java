package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import miniventure.game.GameProtocol.TileUpdate;
import miniventure.game.item.ServerItem;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.ServerMob;
import miniventure.game.world.entity.particle.ItemEntity;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.LevelGenerator;

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
	
	//private float timeCache = 0; // this is used when you should technically be updating < 1 tile in a frame.
	
	private final LevelGenerator levelGenerator;
	
	public ServerLevel(WorldManager world, int levelId, LevelGenerator levelGenerator) {
		super(world, levelId, levelGenerator.generateTiles(), ServerTile::new);
		this.levelGenerator = levelGenerator;
	}
	
	/*@Override
	public void entityMoved(Entity entity) {
		Point prevChunk = entityChunks.get(entity);
		super.entityMoved(entity);
		Point newChunk = entityChunks.get(entity);
		
		if(!Objects.equals(prevChunk, newChunk)) {
			//	System.out.println("Server broadcasting entity "+(newChunk==null?"removal":"addition")+": "+entity);
			if(newChunk != null)
				ServerCore.getServer().broadcast(new EntityAddition(entity), this);
			else
				ServerCore.getServer().broadcast(new EntityRemoval(entity), this);
		}
	}*/
	
	public void onTileUpdate(ServerTile tile) {
		ServerCore.getServer().broadcast(new TileUpdate(tile), this);
		
		HashSet<Tile> tiles = getAreaTiles(tile.getLocation(), 1, true);
		
		synchronized (newTileUpdates) {
			newTileUpdates.addAll(tiles);
		}
	}
	
	//private float updateAllDelta = 0;
	public void update(Entity[] entities, float delta) {
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
		updateEntities(entities, delta);
		
		if(entities.length < getMobCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)].makeMob());
	}
	
	public void dropItems(@NotNull ItemDrop drop, @NotNull WorldObject source, @Nullable WorldObject target) {
		dropItems(drop, source.getCenter(), target == null ? null : target.getCenter());
	}
	public void dropItems(@NotNull ItemDrop drop, Vector2 dropPos, @Nullable Vector2 targetPos) {
		for(ServerItem item: drop.getDroppedItems())
			dropItem(item, dropPos, targetPos);
	}
	
	public void dropItem(@NotNull ServerItem item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) { dropItem(item, false, dropPos, targetPos); }
	public void dropItem(@NotNull ServerItem item, boolean delayPickup, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		ItemEntity ie = new ItemEntity(item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		Tile closest = getClosestTile(dropPos.x, dropPos.y);
		
		Rectangle itemBounds = ie.getBounds();
		itemBounds.setPosition(dropPos);
		
		if(closest == null) {
			System.err.println("ERROR dropping item, closest tile is null");
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
		
		ie = new ItemEntity(item, dropDir, delayPickup);
		
		ie.moveTo(this, dropPos);
		getWorld().setEntityLevel(ie, this);
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
	
	public void spawnMob(ServerMob mob) {
		if(!mob.maySpawn()) return;
		
		int x, y;
		TileTypeEnum type;
		do {
			x = MathUtils.random(getWidth()-1);
			y = MathUtils.random(getHeight()-1);
			Tile tile = getTile(x, y);
			if(tile == null) {
				System.err.println("level contains null tile! "+x+","+y+"@level="+this);
			}
			type = tile.getType().getTypeEnum();
		} while(!mob.maySpawn(type));
		
		// loadChunk(Chunk.getCoords(x, y));
		mob.moveTo(getTile(x, y));
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
