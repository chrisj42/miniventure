package miniventure.game.world;

import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.WorldManager;
import miniventure.game.item.Item;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.particle.ItemEntity;
import miniventure.game.world.entity.mob.AiType;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.MobAi;
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
	
	public ServerLevel(WorldManager world, int depth, LevelGenerator levelGenerator) {
		super(world, depth, levelGenerator.worldWidth, levelGenerator.worldHeight);
		this.levelGenerator = levelGenerator;
	}
	
	@Override
	public void entityMoved(Entity entity) {
		if(getWorld().isKeepAlive(entity)) {
			if(entity.getServerLevel() == this) {
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
		if(loadedChunks.size() == 0) return;
		
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
			spawnMob(new MobAi(AiType.values[MathUtils.random(AiType.values.length-1)]));
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
	
	public ChunkData[] createClientLevel(Player client) {
		// creates a new level instance to send to the new client
		
		Array<Point> points = getAreaChunks(client.getCenter(), 1, true, true);
		
		ChunkData[] chunks = new ChunkData[points.size];
		//Array<EntityData> entities = new Array<>(EntityData.class);
		
		for (int i = 0; i < points.size; i++) {
			Point p = points.get(i);
			Chunk chunk = loadedChunks.containsKey(p) ? loadedChunks.get(p) : new Chunk(p.x, p.y, this, levelGenerator.generateChunk(p.x, p.y));
			chunks[i] = new ChunkData(chunk, this);
		}
		
		return chunks;
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerLevel && super.equals(other); }
	
	
	
	private static final String[] levelNames = {"Surface"};
	private static final int minDepth = 0;
	
	private static ServerLevel[] levels = new ServerLevel[0];
	private static final HashMap<Entity, ServerLevel> entityLevels = new HashMap<>();
	
	public static void clearLevels() {
		entityLevels.clear();
		for(ServerLevel level: levels)
			level.entities.clear();
		levels = new ServerLevel[0];
	}
	
	@Nullable
	public static ServerLevel getLevel(int depth) {
		int idx = depth-minDepth;
		return idx >= 0 && idx < levels.length ? levels[idx] : null;
	}
	
	public static void generateLevels(WorldManager world, LevelGenerator levelGenerator) {
		MenuScreen menu = GameCore.getScreen();
		LoadingScreen display = null;
		if(menu != null && menu instanceof LoadingScreen) {
			display = (LoadingScreen) menu;
			display.pushMessage("");
		}
		
		clearLevels();
		levels = new ServerLevel[levelNames.length];
		for(int i = 0; i < levels.length; i++) {
			log(display, "Loading level "+(i+1)+"/"+levels.length+"...");
			levels[i] = new ServerLevel(world, i + minDepth, levelGenerator);
		}
		if(display != null) display.popMessage();
	}
	private static void log(@Nullable LoadingScreen display, String text) {
		if(display == null)
			System.out.println(text);
		else
			display.editMessage(text);
	}
	
	@Nullable
	public static ServerLevel getEntityLevel(Entity entity) { return entityLevels.get(entity); }
}
