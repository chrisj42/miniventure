package miniventure.game.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerLevel extends Level {
	
	private static final float percentTilesUpdatedPerSecond = 0.02f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	private final Set<Tile> newTileUpdates = Collections.synchronizedSet(new HashSet<>());
	private final Set<Tile> oldTileUpdates = new HashSet<>();
	
	private float timeCache = 0; // this is used when you should technically be updating < 1 tile in a frame.
	
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
	
	public void onTileUpdate(Tile tile) {
		ServerCore.getServer().broadcast(new TileUpdate(tile), this);
		
		List<Tile> tiles = Arrays.asList(getAreaTiles(tile.getLocation(), 1, true).shrink());
		
		synchronized (newTileUpdates) {
			newTileUpdates.addAll(tiles);
		}
	}
	
	public void update(Entity[] entities, float delta) {
		if(getLoadedChunkCount() == 0) return;
		
		// tiles updated
		float tilesPerSecond = percentTilesUpdatedPerSecond * tileCount;
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
		}
		
		// update the tiles in the queue
		Tile[] tilesToUpdate = newTileUpdates.toArray(new Tile[newTileUpdates.size()]);
		synchronized (newTileUpdates) { newTileUpdates.clear(); } // clear it first, so we won't lose any updates added while updating.
		
		boolean[] unfinishedTiles = new boolean[tilesToUpdate.length];
		for(int i = 0; i < tilesToUpdate.length; i++) {
			unfinishedTiles[i] = tilesToUpdate[i].update(delta, true);
		}
		
		// now do old updates
		Tile[] previouslyUpdatedTiles = oldTileUpdates.toArray(new Tile[oldTileUpdates.size()]);
		for(Tile tile: previouslyUpdatedTiles) {
			boolean shouldUpdate = tile.update(delta, false);
			if(!shouldUpdate)
				oldTileUpdates.remove(tile);
		}
		
		// now, add any new tiles that still need updates
		for(int i = 0; i < unfinishedTiles.length; i++) {
			if(unfinishedTiles[i])
				oldTileUpdates.add(tilesToUpdate[i]);
		}
		
		
		// update entities
		updateEntities(entities, delta);
		
		if(entities.length < getEntityCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(new MobAi(AiType.values[MathUtils.random(AiType.values.length-1)]));
	}
	
	@Override
	public Array<Vector3> renderLighting(Rectangle renderSpace) { return new Array<>(); }
	
	
	public void dropItems(@NotNull ItemDrop drop, @NotNull WorldObject source, @Nullable WorldObject target) {
		dropItems(drop, source.getCenter(), target == null ? null : target.getCenter());
	}
	public void dropItems(@NotNull ItemDrop drop, Vector2 dropPos, @Nullable Vector2 targetPos) {
		for(Item item: drop.getDroppedItems())
			dropItem(item, dropPos, targetPos);
	}
	
	public void dropItem(@NotNull Item item, @NotNull Vector2 dropPos, @Nullable Vector2 targetPos) {
		
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
			Array<Tile> adjacent = closest.getAdjacentTiles(true);
			Boundable.sortByDistance(adjacent, targetPos == null ? dropPos : targetPos);
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
			dropDir = targetPos.sub(dropPos);
		
		ie = new ItemEntity(item, dropDir);
		
		ie.moveTo(this, dropPos);
		getWorld().setEntityLevel(ie, this);
	}
	
	
	private void spawnMob(ServerMob mob, Tile[] tiles) {
		if(tiles.length == 0) throw new IllegalArgumentException("Tile array for spawning mobs must have at least one tile in it. (tried to spawn mob "+mob+")");
		
		Tile spawnTile;
		if(tiles.length == 1)
			spawnTile = tiles[0];
		else {
			do spawnTile = tiles[MathUtils.random(tiles.length - 1)];
			while (spawnTile == null || !mob.maySpawn(spawnTile.getType()));
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
			int x, y;
			TileType type;
			do {
				x = MathUtils.random((int)spawnArea.x, (int) (spawnArea.x+spawnArea.width));
				y = MathUtils.random((int)spawnArea.y, (int) (spawnArea.y+spawnArea.height));
				Tile tile = getTile(x, y);
				if(tile == null) {
					TileType[] types = levelGenerator.generateTile(x, y);
					type = types[types.length-1];
				} else
					type = tile.getType();
			} while(!mob.maySpawn(type));
			
			loadChunk(Chunk.getCoords(x, y));
			Tile spawnTile = getTile(x, y);
			//noinspection ConstantConditions
			mob.moveTo(spawnTile);
		}
	}
	
	// note that asking for unloaded chunks will load them.
	public Array<Chunk> getAreaChunks(Vector2 tilePos, int radius, boolean loaded, boolean unloaded) {
		return getAreaChunks(Chunk.getCoords(tilePos), radius, loaded, unloaded);
	}
	public Array<Chunk> getAreaChunks(Point chunkCoords, int radius, boolean loaded, boolean unloaded) {
		Array<Point> coords = getAreaChunkCoords(chunkCoords.x, chunkCoords.y, radius, loaded, unloaded);
		Array<Chunk> chunks = new Array<>(false, coords.size, Chunk.class);
		for(Point p: coords) {
			if(!isChunkLoaded(p)) loadChunk(p);
			chunks.add(getLoadedChunk(p));
		}
		
		return chunks;
	}
	
	@Override
	ServerTile createTile(int x, int y, TileType[] types, String[] data) { return new ServerTile(this, x, y, types, data); }
	
	@Override
	void loadChunk(Point chunkCoord) {
		// TODO this will need to get redone when loading from file
		
		if(isChunkLoaded(chunkCoord)) return;
		
		loadChunk(new Chunk(chunkCoord.x, chunkCoord.y, this, levelGenerator.generateChunk(chunkCoord.x, chunkCoord.y)));
	}
	
	@Override
	void unloadChunk(Point chunkCoord) {
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
