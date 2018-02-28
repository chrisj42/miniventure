package miniventure.game.world;

import java.util.Arrays;

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

public class ServerLevel extends Level {
	
	private static final float percentTilesUpdatedPerSecond = 2f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	private final LevelGenerator levelGenerator;
	
	public ServerLevel(int depth, LevelGenerator levelGenerator) {
		super(depth, levelGenerator.worldWidth, levelGenerator.worldHeight);
		this.levelGenerator = levelGenerator;
	}
	
	@Override
	public void entityMoved(Entity entity) {
		if(GameCore.getWorld().isKeepAlive(entity)) {
			if(entity.getLevel() == this) {
				// load all surrounding chunks
				for (Point p : getAreaChunks(entity.getCenter(), 1, false, true)) {
					Chunk newChunk = new Chunk(p.x, p.y, this, levelGenerator.generateChunk(p.x, p.y));
					tileCount += newChunk.width * newChunk.height;
					loadedChunks.put(p, newChunk);
				}
			}
		}
		
		super.entityMoved(entity);
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
		
		if(this.entities.size() < getEntityCap() && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)].makeMob());
	}
	
	@Override
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {}
	@Override
	public Array<Vector3> renderLighting(Rectangle renderSpace) { return new Array<>(); }
	
	
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
			WorldObject.sortByDistance(adjacent, targetPos == null ? dropPos : targetPos);
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
		addEntity(ie);
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
	
	// only spawns within the given area
	public void spawnMob(Mob mob, Rectangle spawnArea) { spawnMob(mob, spawnArea, true); }
	public void spawnMob(Mob mob, Rectangle spawnArea, boolean loadedChunksOnly) {
		// if the mob is a keepAlive mob, then unloaded tiles are considered; otherwise, they are not.
		if(loadedChunksOnly)
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
	
	public Level createClientLevel(Player client) {
		// creates a new level instance to send to the new client
		Level level = new Level();
		
		Rectangle bounds = null;
		
		for (Point p : getAreaChunks(client.getCenter(), 1, true, true)) {
			Chunk chunk = loadedChunks.containsKey(p) ? loadedChunks.get(p) : new Chunk(p.x, p.y, this, levelGenerator.generateChunk(p.x, p.y));
			level.tileCount += chunk.width * chunk.height;
			level.loadedChunks.put(p, chunk);
			Rectangle chunkBounds = chunk.getBounds();
			if(bounds == null) bounds = chunkBounds;
			else bounds.merge(chunkBounds);
		}
		
		level.entities.addAll(Arrays.asList(getOverlappingEntities(bounds, client).shrink()));
		
		return level;
	}
	
	@Nullable
	public static ServerLevel getLevel(int depth) {
		return (ServerLevel) Level.getLevel(depth);
	}
	
	public static void resetLevels(LevelGenerator levelGenerator) { resetLevels(null, levelGenerator); }
	public static void resetLevels(@Nullable LoadingScreen display, LevelGenerator levelGenerator) {
		clearLevels();
		levels = new ServerLevel[levelNames.length];
		if(display != null) display.pushMessage("Loading level 0/"+levels.length+"...");
		for(int i = 0; i < levels.length; i++) {
			if(display != null) display.editMessage("Loading level "+(i+1)+"/"+levels.length+"...");
			levels[i] = new ServerLevel(i + minDepth, levelGenerator);
		}
		if(display != null) display.popMessage();
	}
}
