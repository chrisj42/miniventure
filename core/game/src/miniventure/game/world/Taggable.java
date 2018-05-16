package miniventure.game.world;

public interface Taggable<T> {
	
	interface Tag<T> {
		T getObject(WorldManager world);
	}
	
	Tag<? extends T> getTag();
	
}
