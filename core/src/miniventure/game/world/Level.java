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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Level {
	
	private static final String[] levelNames = {"Surface"};
	private static final int minDepth = 0;
	
	private static Level[] levels = new Level[0];
	private static final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	private static final float percentTilesUpdatedPerSecond = 2f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	public static void resetLevels(LoadingScreen display) {
		entityLevels.clear();
		for(Level level: levels)
			level.entities.clear();
		
		levels = new Level[levelNames.length];
		int msg = display.addIncrement("Loading level 0/"+levels.length+"...");
		for(int i = 0; i < levels.length; i++) {
			display.setMessage(msg, "Loading level "+(i+1)+"/"+levels.length+"...");
			levels[i] = new Level(i + minDepth, 256, 256);
		}
		display.removeMessage(msg);
	}
	
	public static Level getLevel(int depth) { return levels[depth-minDepth]; }
	
	@Nullable
	public static Level getEntityLevel(Entity entity) { return entityLevels.get(entity); }
	
	
	
	private final int width, height;
	private final Tile[][] tiles;
	
	private final HashSet<Entity> entities = new HashSet<>();
	
	private int entityCap = 50;
	
	public Level(int depth, int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new Tile[width][height];
		
		TileType[][] tileTypes = LevelGenerator.generateLevel(width, height);
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = new Tile(this, x, y, tileTypes[x][y]);
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getEntityCap() { return entityCap; }
	
	public int getEntityCount() { return entities.size(); }
	
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
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e);
		if (entityLevels.get(e) == this)
			entityLevels.remove(e);
	}
	
	public void update(float delta) {
		int tilesToUpdate = (int) (percentTilesUpdatedPerSecond * width*height * delta);
		
		for(int i = 0; i < tilesToUpdate; i++) {
			int x = MathUtils.random(width-1);
			int y = MathUtils.random(height-1);
			tiles[x][y].update(delta);
		}
		
		// update entities
		Entity[] entities = this.entities.toArray(new Entity[this.entities.size()]);
		for(Entity e: entities)
			e.update(delta);
		
		if(this.entities.size() < entityCap && MathUtils.randomBoolean(0.01f))
			spawnMob(AiType.values[MathUtils.random(AiType.values.length-1)].makeMob());
	}
	
	public void spawnMob(Mob mob) {
		Tile spawnTile;
		do spawnTile = getTile(
			MathUtils.random(getWidth()-1),
			MathUtils.random(getHeight()-1)
		);
		while(spawnTile == null || !mob.maySpawn(spawnTile));
		
		mob.moveTo(spawnTile);
		addEntity(mob);
	}
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta) {
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(renderSpace)); // tiles first
		objects.addAll(getOverlappingEntities(renderSpace)); // entities second
		
		for(WorldObject obj: objects)
			obj.render(batch, delta);
	}
	
	public Array<Vector3> renderLighting(Rectangle renderSpace) {
		final TextureRegion light = GameCore.icons.get("light");
		
		Rectangle expandedSpace = new Rectangle(renderSpace.x - Tile.SIZE*10, renderSpace.y - Tile.SIZE*10, renderSpace.width+Tile.SIZE*10*2, renderSpace.height+Tile.SIZE*10*2);
		
		Array<WorldObject> objects = new Array<>();
		objects.addAll(getOverlappingTiles(expandedSpace));
		objects.addAll(getOverlappingEntities(expandedSpace));
		
		Array<Vector3> lighting = new Array<>();
		
		for(WorldObject obj: objects) {
			float lightR = obj.getLightRadius();
			if(lightR > 0) {
				lighting.add(new Vector3(obj.getBounds().getCenter(new Vector2()), lightR));
				// so apparently the light draws with the origin for the image in the upper left, rather than the bottom left. No clue why.
				//Vector2 center = obj.getBounds().getCenter(new Vector2());
				//System.out.println("drawing light at " + (center.x-renderSpace.x-lightR)+","+(center.y-renderSpace.y-lightR));
				//batch.draw(light, center.x-renderSpace.x-lightR, renderSpace.height - (center.y-renderSpace.y)-lightR, lightR*2, lightR*2);
			}
		}
		
		return lighting;
	}
	
	public void dropItem(@NotNull Item item, Vector2 dropPos, @Nullable Vector2 targetPos) {
		
		/* this drops the itemEntity at the given coordinates, with the given direction (random if null).
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
		Rectangle itemBounds = new Rectangle(dropPos.x, dropPos.y, item.getTexture().getRegionWidth(), item.getTexture().getRegionHeight());
		Tile closest = getClosestTile(itemBounds);
		
		if(closest == null) {
			System.err.println("ERROR dropping item, closest tile is null");
			return;
		}
		
		ItemEntity ie = new ItemEntity(item, Vector2.Zero.cpy()); // this is a dummy variable.
		
		if(!closest.isPermeableBy(ie)) {
			// we need to look around for a tile that the item *can* be placed on.
			Array<Tile> adjacent = closest.getAdjacentTiles(true);
			Tile.sortByDistance(adjacent, targetPos);
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
		
		if(targetPos == null)
			targetPos = dropPos.cpy().add(new Vector2(MathUtils.random(Tile.SIZE/2), MathUtils.random(Tile.SIZE/2)));
		
		ie = new ItemEntity(item, targetPos.sub(dropPos));
		
		ie.moveTo(this, dropPos);
		addEntity(ie);
	}
	
	@Nullable
	public Tile getTile(int xt, int yt) {
		if(xt >= 0 && xt < width && yt >= 0 && yt < height)
			return tiles[xt][yt];
		return null;
	}
	
	public Array<Tile> getOverlappingTiles(Rectangle entityRect) {
		int tileMinX = (int) entityRect.x / Tile.SIZE;
		int tileMaxX = (int) (entityRect.x + entityRect.width) / Tile.SIZE;
		int tileMinY = (int) entityRect.y / Tile.SIZE;
		int tileMaxY = (int) (entityRect.y + entityRect.height) / Tile.SIZE;
		
		Array<Tile> overlappingTiles = new Array<>();
		for(int x = Math.max(0, tileMinX); x <= Math.min(width-1, tileMaxX); x++)
			for(int y = Math.max(0, tileMinY); y <= Math.min(height-1, tileMaxY); y++)
				overlappingTiles.add(tiles[x][y]);
		
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
	
	public Array<Tile> getAreaTiles(int xt, int yt, int radius, boolean includeCenter) {
		Array<Tile> tiles = new Array<>();
		for(int x = Math.max(0, xt-radius); x <= Math.min(width-1, xt+radius); x++) {
			for(int y = Math.max(0, yt-radius); y <= Math.min(height-1, yt+radius); y++) {
				tiles.add(this.tiles[x][y]);
			}
		}
		
		if(!includeCenter)
			tiles.removeValue(this.tiles[xt][yt], true);
		
		return tiles;
	}
	
	@Nullable
	public Tile getClosestTile(Rectangle area) {
		Vector2 center = new Vector2();
		area.getCenter(center);
		int x = (int)center.x;
		int y = (int)center.y;
		x /= Tile.SIZE;
		y /= Tile.SIZE;
		
		return getTile(x, y);
	}
	
	@Nullable
	public Player getClosestPlayer(final Vector2 pos) {
		Array<Player> players = new Array<>();
		for(Entity e: entities)
			if(e instanceof Player)
				players.add((Player)e);
		
		if(players.size == 0) return null;
		
		players.sort((p1, p2) -> {
			Vector2 p1Pos = p1.getBounds().getCenter(new Vector2());
			Vector2 p2Pos = p2.getBounds().getCenter(new Vector2());
			
			return Float.compare(p1Pos.dst(pos), p2Pos.dst(pos));
		});
		
		return players.get(0);
	}
}
