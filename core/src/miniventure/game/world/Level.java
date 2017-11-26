package miniventure.game.world;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class Level {
	
	private static Level[] levels = new Level[0];
	private static final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	public static void resetLevels() {
		entityLevels.clear();
		for(Level level: levels)
			level.entities.clear();
		
		levels = new Level[1];
		levels[0] = new Level(GameCore.SCREEN_WIDTH / Tile.SIZE, GameCore.SCREEN_HEIGHT / Tile.SIZE);
	}
	
	public static Level getLevel(int idx) { return levels[idx]; }
	
	@Nullable
	public static Level getEntityLevel(Entity entity) { return entityLevels.get(entity); }
	
	
	
	
	private final int width, height;
	private final Tile[] tiles;
	
	private HashSet<Entity> entities = new HashSet<>();
	
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new Tile[width*height];
		
		for(int i = 0; i < tiles.length; i++)
			tiles[i] = new Tile((i%width<5||width-(i%width)<5?TileType.TREE:TileType.GRASS), this, i%width, i/width);
		
		//tiles[5].resetTile(TileType.TREE); // for some variety
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
		Level oldLevel = entityLevels.put(e, this); // replaces the level for the entity
		if(oldLevel != null && oldLevel != this)
			oldLevel.removeEntity(e); // remove it from the other level's entity set.
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e);
		if(entityLevels.get(e) == this)
			entityLevels.remove(e);
	}
	
	public void update(float delta) {
		// TO-DO update random tiles
		
		// update entities
		for(Entity e: entities)
			e.update(delta);
	}
	
	public void render(Player mainPlayer, SpriteBatch batch, float delta) {
		// the game renders around the main player. For now, the level shall be the same size as the screen, so no camera fanciness or coordinate manipulation is needed.
		
		//batch.disableBlending(); // this prevents alpha from being rendered, which gives a performance boost. When drawing tiles, we don't need alpha (yet), so we'll disable it. 
		for(Tile tile: tiles)
			tile.render(batch, delta);
		//batch.enableBlending(); // re-enable alpha for the drawing of entities.
		
		for(Entity entity: entities)
			entity.render(batch, delta);
	}
	
	public void dropItem(Item item, int x, int y, Entity target) {
		// this tries to drop an item toward an entity.
		// 
	}
	
	public void dropItem(Item item, int x, int y) {
		/* this drops the itemEntity at the given coordinate, with any direction.
		 	However, if the given coordinates reside within a solid tileold, the adjacent tiles are checked.
		 		If all surrounding tiles are solid, then it just uses the given coordinates.
		 		But if it finds a non-solid tileold, it drops it towards the non-solid tileold.
		  */
		
	}
	
	
	public Array<Tile> getOverlappingTiles(Rectangle entityRect) {
		int tileMinX = (int) entityRect.x / Tile.SIZE;
		int tileMaxX = (int) (entityRect.x + entityRect.width) / Tile.SIZE;
		int tileMinY = (int) entityRect.y / Tile.SIZE;
		int tileMaxY = (int) (entityRect.y + entityRect.height) / Tile.SIZE;
		
		Array<Tile> overlappingTiles = new Array<>();
		for(int x = tileMinX; x <= tileMaxX && x < width; x++)
			for(int y = tileMinY; y <= tileMaxY && y < height; y++)
				overlappingTiles.add(tiles[x + y*width]);
		
		return overlappingTiles;
	}
	
	public Array<Entity> getOverlappingEntities(Rectangle rect) {
		Array<Entity> overlapping = new Array<>();
		for(Entity entity: entities)
			if(entity.getBounds().overlaps(rect))
				overlapping.add(entity);
		
		return overlapping;
	}
	
	@Nullable
	public Tile getClosestTile(Rectangle area) {
		Vector2 center = new Vector2();
		area.getCenter(center);
		int x = (int)center.x;
		int y = (int)center.y;
		x /= Tile.SIZE;
		y /= Tile.SIZE;
		
		if(x < 0 || x >= width || y < 0 || y >= height) return null;
		else return tiles[x + y * width];
	}
}
