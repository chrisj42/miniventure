package miniventure.game.world;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ItemEntity;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Level {
	
	private static Level[] levels = new Level[0];
	private static final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	private static final float percentTilesUpdatedPerSecond = 2f; // this represents the percent of the total number of tiles in the map that are updated per second.
	
	public static void resetLevels() {
		entityLevels.clear();
		for(Level level: levels)
			level.entities.clear();
		
		levels = new Level[1];
		levels[0] = new Level(256, 256);
	}
	
	public static Level getLevel(int idx) { return levels[idx]; }
	
	@Nullable
	public static Level getEntityLevel(Entity entity) { return entityLevels.get(entity); }
	
	
	
	private final int width, height;
	private final Tile[][] tiles;
	
	private final HashSet<Entity> entities = new HashSet<>();
	//private final HashSet<Entity> entitiesToAdd = new HashSet<>();
	//private final HashSet<Entity> entitiesToRemove = new HashSet<>();
	
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new Tile[width][height];
		
		TileType[][] tileTypes = LevelGenerator.generateLevel(width, height);
		for(int x = 0; x < tiles.length; x++)
			for(int y = 0; y < tiles[x].length; y++)
				tiles[x][y] = new Tile(tileTypes[x][y]/*(x<5?TileType.TREE:width-x<5? TileType.CACTUS:TileType.GRASS)*/, this, x, y);
		
		//tiles[5][0].resetTile(TileType.WATER);
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	
	public void addEntity(Entity e) {
		//System.out.println("adding entity " + e + " to level " + this + " at " + e.getBounds().x+","+e.getBounds().y);
		//synchronized (entitiesToAdd) {
		entities.add(e);
		//	entitiesToAdd.add(e);
			Level oldLevel = entityLevels.put(e, this); // replaces the level for the entity
			if (oldLevel != null && oldLevel != this)
				oldLevel.removeEntity(e); // remove it from the other level's entity set.
		//}
	}
	
	public void removeEntity(Entity e) {
		//System.out.println("removing entity "+e+" from level "+this);
		//synchronized (entitiesToRemove) {
			//entitiesToRemove.add(e);
			entities.remove(e);
			if (entityLevels.get(e) == this)
				entityLevels.remove(e);
		//}
	}
	
	public void update(float delta) {
		int tilesToUpdate = (int) (percentTilesUpdatedPerSecond * width*height * delta);
		
		for(int i = 0; i < tilesToUpdate; i++) {
			int x = MathUtils.random(width-1);
			int y = MathUtils.random(height-1);
			tiles[x][y].update(delta);
		}
		
		//System.out.println("entities tracked: " + entities);
		
		// update entities
		Entity[] entities = this.entities.toArray(new Entity[this.entities.size()]);
		for(Entity e: entities)
			e.update(delta);
		
		/*synchronized (entitiesToAdd) {
			for (Entity e : entitiesToAdd) {
				for (Level level : levels)
					level.entities.remove(e);
				entities.add(e);
				e.addedToLevel(this);
			}
			
			entitiesToAdd.clear();
		}
		
		synchronized (entitiesToRemove) {
			for (Entity e : entitiesToRemove)
				entities.remove(e);
			entitiesToRemove.clear();
		}*/
	}
	
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta) {
		// the game renders around the main player. For now, the level shall be the same size as the screen, so no camera fanciness or coordinate manipulation is needed.
		
		//batch.disableBlending(); // this prevents alpha from being rendered, which gives a performance boost. When drawing tiles, we don't need alpha (yet), so we'll disable it.
		//for(Tile[] tiles: this.tiles)
			for(Tile tile: getOverlappingTiles(renderSpace))
				tile.render(batch, delta);
		//batch.enableBlending(); // re-enable alpha for the drawing of entities.
		
		Array<Entity> overlapping = getOverlappingEntities(renderSpace);
		//System.out.println("entities being rendered: " + overlapping);
		for(Entity entity: overlapping)
			entity.render(batch, delta);
	}
	
	public void dropItem(@NotNull Item item, int x, int y, @NotNull Entity target) {
		// this tries to drop an item toward an entity.
		// 
		//System.out.println("dropping item " + item + " towards " + target);
		
		Vector2 itemPos = new Vector2(x, y);
		Vector2 targetPos = new Vector2();
		target.getBounds().getCenter(targetPos);
		ItemEntity ie = new ItemEntity(item, targetPos.sub(itemPos));
		ie.moveTo(this, x, y);
		addEntity(ie);
	}
	
	public void dropItem(@NotNull Item item, int xt, int yt) {
		/* this drops the itemEntity at the given coordinate, with any direction.
		 	However, if the given coordinates reside within a solid tile, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tile, it drops it towards the non-solid tile.
		  */
		
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
		Array<Entity> overlapping = new Array<>();
		for(Entity entity: entities)
			if(entity.getBounds().overlaps(rect))
				overlapping.add(entity);
		
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
}
