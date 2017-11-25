package miniventure.game.world;

import java.util.HashSet;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Level {
	
	private final int width, height;
	private final Tile[] tiles;
	
	private HashSet<Entity> entities = new HashSet<>();
	
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new Tile[width*height];
		
		for(int i = 0; i < tiles.length; i++) {
			tiles[i] = new Tile(TileType.GRASS, this, i%width, i/width);
		}
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void update(float delta) {
		// TO-DO pollAnimation random tiles
		
		// pollAnimation entities
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
	
}
