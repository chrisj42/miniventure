package miniventure.game.world;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;

public class Level {
	
	public Level() {
		
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
