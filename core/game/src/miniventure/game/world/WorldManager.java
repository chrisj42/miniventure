package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.GameProtocol;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public abstract class WorldManager {
	
	private final HashMap<Integer, Entity> entityIDMap = new HashMap<>();
	protected float gameTime;
	
	private final HashMap<Integer, Level> levels = new HashMap<>();
	private final HashMap<Level, Set<Entity>> levelEntities = new HashMap<>();
	private final HashMap<Entity, Level> entityLevels = new HashMap<>();
	
	@FunctionalInterface
	interface EntitySetAction {
		void actOnSet(Set<Entity> set);
	}
	
	@FunctionalInterface
	interface EntitySetFunction<T> {
		T getFromSet(Set<Entity> set);
	}
	
	
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
	
	
	/** @noinspection MismatchedQueryAndUpdateOfCollection*/
	private static final HashSet<Entity> emptySet = new HashSet<>();
	
	void actOnEntitySet(Level level, EntitySetAction action) {
		Set<Entity> entitySet = levelEntities.getOrDefault(level, emptySet);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (entitySet) {
			action.actOnSet(entitySet);
		}
	}
	
	<T> T getFromEntitySet(Level level, EntitySetFunction<T> getter) {
		Set<Entity> entitySet = levelEntities.getOrDefault(level, emptySet);
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (entitySet) {
			return getter.getFromSet(entitySet);
		}
	}
	
	public int getLevelCount() { return levelEntities.size(); }
	public int getEntityCount(Level level) {
		return getFromEntitySet(level, set -> set.size());
	}
	
	protected Entity[] getEntities(Level level) {
		return getFromEntitySet(level, set -> set.toArray(new Entity[set.size()]));
	}
	
	protected void addLevel(@NotNull Level level) {
		levelEntities.put(level, Collections.synchronizedSet(new HashSet<>()));
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
	
	
	public void registerEntity(Entity e) {
		System.out.println(this+": registered entity "+e);
		entityIDMap.put(e.getId(), e);
	}
	
	/** generates a entity id that is unique for this game. */
	public int registerEntityWithNewId(Entity e) {
		int eid;
		
		do eid = MathUtils.random.nextInt();
		while(entityIDMap.containsKey(eid));
		
		entityIDMap.put(eid, e);
		return eid;
	}
	
	public void deregisterEntity(int eid) {
		Entity e = entityIDMap.get(eid);
		Level level = entityLevels.get(e);
		
		actOnEntitySet(level, set -> {
			entityLevels.remove(e);
			set.remove(e);
			entityIDMap.remove(eid);
		});
		System.out.println(this+": deregistered entity "+e);
	}
	
	public void setEntityLevel(Entity e, @NotNull Level level) {
		if(!entityIDMap.containsKey(e.getId()) || !levels.containsKey(level.getDepth())) {
			System.err.println(this + ": couldn't set entity level, entity " + e + " or level " + level + " is not registered. Ignoring request.");
			return;
		}
		
		Level oldLevel = entityLevels.put(e, level);
		System.out.println("for "+this+": setting level of entity " + e + " to " + level + " (removing from level "+oldLevel+")");
		
		if(!level.equals(oldLevel)) {
			actOnEntitySet(level, set -> set.add(e));
			actOnEntitySet(oldLevel, set -> set.remove(e));
		}
		
		level.entityMoved(e, true);
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
