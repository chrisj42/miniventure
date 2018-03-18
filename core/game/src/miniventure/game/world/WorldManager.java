package miniventure.game.world;

import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameProtocol;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class WorldManager {
	
	private final HashMap<Integer, Entity> entityIDMap = new HashMap<>();
	protected float gameTime;
	
	private final HashMap<Integer, Level> levels = new HashMap<>();
	private final HashMap<Level, HashSet<Entity>> levelEntities = new HashMap<>();
	private final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	// this class contains all the levels in the game.
	public WorldManager() {
		
	}
	
	
	/** update the world's game logic. (can also use to render) */
	public abstract void update(float delta);
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	/** if there is a world loaded (should be true after calling createWorld) */
	public abstract boolean worldLoaded();
	
	/** creates the world */
	public abstract void createWorld(int width, int height);
	
	/* load world from file */
	/*default void createWorld(File worldPath) {
		
	}*/
	
	/** unload and close the world, possibly saving to file */
	public void exitWorld() { exitWorld(true); }
	public abstract void exitWorld(boolean save);
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	public int getLevelCount() { return levelEntities.size(); }
	
	protected void addLevel(@NotNull Level level) {
		levelEntities.put(level, new HashSet<>());
		levels.put(level.getDepth(), level);
	}
	
	// I might not ever actually need this.
	protected void clearLevels() {
		entityLevels.clear();
		levelEntities.clear();
		levels.clear();
		entityIDMap.clear();
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	public void registerEntity(Entity e, int eid) {
		entityIDMap.put(eid, e);
	}
	
	/** generates a entity id that is unique for this game. */
	public int registerEntity(Entity e) {
		int eid;
		
		do eid = MathUtils.random.nextInt();
		while(entityIDMap.containsKey(eid));
		
		registerEntity(e, eid);
		return eid;
	}
	
	public void setEntityLevel(Entity e, Level level) {
		if(!entityIDMap.containsKey(e.getId()) || !levels.containsKey(level.getDepth()))
			throw new IllegalArgumentException("couldn't set entity level, entity or level is not registered.");
		
		Level oldLevel = entityLevels.put(e, level);
		if(levelEntities.containsKey(oldLevel))
			levelEntities.get(oldLevel).remove(e);
		
		levelEntities.get(level).add(e);
		
		level.entityMoved(e);
	}
	
	public void deregisterEntity(int eid) {
		Entity e = entityIDMap.get(eid);
		entityLevels.remove(e);
		entityIDMap.remove(eid);
	}
	
	/** should the level keep chunks around this object loaded? */
	public abstract boolean isKeepAlive(WorldObject obj);
	
	
	/*  --- GET METHODS --- */
	
	
	/** get all keep-alive objects on the given level */
	public abstract Array<WorldObject> getKeepAlives(Level level);
	
	public Entity getEntity(int eid) { return entityIDMap.get(eid); }
	
	public Level getLevel(int depth) { return levels.get(depth); }
	
	public Level getEntityLevel(Entity e) { return entityLevels.get(e); }
	
	public abstract GameProtocol getSender();
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	public float getGameTime() { return gameTime; }
	
	public String getTimeString() { return TimeOfDay.getTimeString(gameTime); }
}
