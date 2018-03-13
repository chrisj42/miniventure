package miniventure.game;

import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.utils.Array;

public interface WorldManager {
	
	// this class contains all the levels in the game.
	
	/** creates the world */
	void createWorld(int width, int height);
	
	/* load world from file */
	//void createWorld(File worldPath);
	
	/** unload and close the world, possibly saving to file */
	default void exitWorld() { exitWorld(true); }
	void exitWorld(boolean save);
	
	/** if there is a world loaded (should be true after calling createWorld) */
	boolean worldLoaded();
	
	/** update the world's game logic. (can also use to render) */
	void update(float delta);
	
	/** should the level keep chunks around this object loaded? */
	boolean isKeepAlive(WorldObject obj);
	
	/** get all keep-alive objects on the given level */
	Array<WorldObject> getKeepAlives(Level level);
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	float getGameTime(); 
	
	/** generates a entity id that is unique for this game. */
	int generateEntityID(Entity entity);
	
	default String getTimeString() { return TimeOfDay.getTimeString(getGameTime()); }
}
