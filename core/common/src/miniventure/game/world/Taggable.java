package miniventure.game.world;

import miniventure.game.world.management.WorldManager;

public interface Taggable<T> {
	
	interface Tag<T> {
		T getObject(WorldManager world);
	}
	
	Tag<? extends T> getTag();
	
}
