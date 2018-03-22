package miniventure.game.world;

import java.util.Map;

import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.item.Item;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.MobAi;
import miniventure.game.world.entity.mob.Player;
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
	
	private static final float percentTilesUpdatedPerSecond = 2f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
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
		
		if(newChunk != prevChunk)
			ServerCore.getServer().broadcast(new EntityAddition(entity), this);
	}
	
	public void update(Entity[] entities, float delta) {
		if(getLoadedChunkCount() == 0) return;
		
		int tilesToUpdate = (int) (percentTilesUpdatedPerSecond * tileCount * delta);
		
		Chunk[] chunks = getLoadedChunkArray();
		for(int i = 0; i < tilesToUpdate; i++) {
			Chunk chunk = (Chunk) chunks[MathUtils.random(chunks.length-1)];
			int x = MathUtils.random(chunk.width-1);
			int y = MathUtils.random(chunk.height-1);
			Tile t = chunk.getTile(x, y);
			if(t != null) t.update(delta);
		}
		
		// update entities
		updateEntities(entities, delta);
		
		if(entities.length < getEntityCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(new MobAi(getWorld(), AiType.values[MathUtils.random(AiType.values.length-1)]));
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
	}
	
	public void spawnMob(Mob mob) {
		// only spawns on loaded chunks
		Array<Tile> tiles = new Array<>(false, getLoadedChunkCount() * Chunk.SIZE * Chunk.SIZE, Tile.class);
		for(Chunk chunk: loadedChunks.get(Map::values))
			for(Tile[] chunkTiles: chunk.getTiles())
				tiles.addAll(chunkTiles);
		
		tiles.shrink();
		
		spawnMob(mob, tiles.toArray(ServerTile.class));
	}
	
	// only spawns within the given area
	public void spawnMob(Mob mob, Rectangle spawnArea) {
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
	
	@Override
	ServerTile createTile(int x, int y, TileType[] types, String[] data) { return new ServerTile(this, x, y, types, data); }
	
	@Override
	void loadChunk(Point chunkCoord) {
		// TODO this will need to get redone when loading from file
		
		if(isChunkLoaded(chunkCoord)) return;
		
		putLoadedChunk(chunkCoord, new Chunk(chunkCoord.x, chunkCoord.y, this, levelGenerator.generateChunk(chunkCoord.x, chunkCoord.y)));
	}
	
	@Override
	void unloadChunk(Point chunkCoord) {
		// TODO this will need to get redone when saving to file
		
		Chunk chunk = getLoadedChunk(chunkCoord);
		if(chunk == null) return; // already unloaded
		
		for(Entity e: entityChunks.keySet().toArray(new Entity[entityChunks.size()]))
			if(entityChunks.get(e).equals(chunkCoord))
				e.remove();
		
		loadedChunks.access(chunks -> chunks.remove(chunkCoord));
	}
	
	public ChunkData[] createClientLevel(Player client) {
		// creates a new level instance to send to the new client
		
		Array<Point> points = getAreaChunks(client.getCenter(), 1, true, true);
		
		ChunkData[] chunks = new ChunkData[points.size];
		//Array<EntityData> entities = new Array<>(EntityData.class);
		
		for (int i = 0; i < points.size; i++) {
			Point p = points.get(i);
			chunks[i] = new ChunkData(getChunk(p.x, p.y), this);
		}
		
		return chunks;
	}
	
	@NotNull
	public Chunk getChunk(int cx, int cy) {
		Point p = new Point(cx, cy);
		Chunk chunk = getLoadedChunk(p);
		if(chunk == null) {
			chunk = new Chunk(cx, cy, this, levelGenerator.generateChunk(cx, cy));
			putLoadedChunk(p, chunk);
		}
		
		return chunk;
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerLevel && super.equals(other); }
	
	@Override
	public String toString() { return "Server"+super.toString(); }
}
