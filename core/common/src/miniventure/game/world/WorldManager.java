package miniventure.game.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.TileEnumMapper;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

/**
 * this class contains all the levels in the game, and generally manages world-level data.
 */
public abstract class WorldManager {
	
	private static final int INITIAL_ENTITY_BUFFER = 128;
	
	protected float gameTime, daylightOffset;
	private final HashMap<Integer, Entity> entityIDMap = new HashMap<>(INITIAL_ENTITY_BUFFER);
	
	private final HashMap<Integer, Level> levels = new HashMap<>(INITIAL_ENTITY_BUFFER);
	private final HashMap<Level, Set<Entity>> levelEntities = new HashMap<>(4);
	private final HashMap<Entity, Level> entityLevels = new HashMap<>(INITIAL_ENTITY_BUFFER);
	
	@FunctionalInterface
	interface EntitySetAction {
		void actOnSet(Set<Entity> set);
	}
	
	@FunctionalInterface
	interface EntitySetFunction<T> {
		T getFromSet(Set<Entity> set);
	}
	
	
	/** update the world's game logic. (can also use to render) */
	public void update(float delta) {
		gameTime += delta;
		if(doDaylightCycle())
			daylightOffset = (daylightOffset + delta) % TimeOfDay.SECONDS_IN_DAY;
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	protected abstract boolean doDaylightCycle();
	
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
		return getFromEntitySet(level, Set::size);
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
		//System.out.println(this+": registered entity "+e);
		entityIDMap.put(e.getId(), e);
		//if(old != null)
		//	throw new IllegalStateException("Attempted to register entity with duplicate entity id "+e.getId()+"; original="+old+", new="+e);
	}
	
	/**
	 * generates a entity id that is unique for this game.
	 * Negative numbers represent entities updated only by the client.
	 */
	public int registerEntityWithNewId(@NotNull Entity e, boolean negative) {
		int eid;
		
		do eid = MathUtils.random.nextInt();
		while((eid < 0) != negative || entityIDMap.containsKey(eid));
		
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
		//System.out.println(this+": deregistered entity "+e);
		
		if(e != null && level != null)
			level.entityMoved(e);
	}
	
	// removes entity from levels, without deregistering it. Can only be done for keep alives.
	protected void removeFromLevels(@NotNull Entity e) {
		if(!isKeepAlive(e))
			System.err.println(this+": entity "+e+" is not a keep-alive; will not remove from levels without deregistering.");
		else {
			Level level = e.getLevel();
			if(level != null)
				actOnEntitySet(level, set -> {
					entityLevels.put(e, null);
					set.remove(e);
				});
		}
	}
	
	public void setEntityLevel(@NotNull Entity e, @NotNull Level level) {
		if(!entityIDMap.containsKey(e.getId()) || !levels.containsKey(level.getDepth())) {
			System.err.println(this + ": couldn't set entity level, entity " + e + " or level " + level + " is not registered. Ignoring request.");
			return;
		}
		
		Level oldLevel = entityLevels.put(e, level);
		//if(e instanceof Player) // so it doesn't go too crazy
		//	System.out.println("for "+this+": setting level of entity " + e + " to " + level + " (removing from level "+oldLevel+") - entity location = " + e.getLocation(true));
		
		if(!level.equals(oldLevel)) {
			actOnEntitySet(level, set -> set.add(e));
			actOnEntitySet(oldLevel, set -> set.remove(e));
		}
		
		level.entityMoved(e);
	}
	
	/** should the level keep chunks around this object loaded? */
	public abstract boolean isKeepAlive(WorldObject obj);
	
	
	/*  --- GET METHODS --- */
	
	
	/** get all keep-alive objects on the given level */
	public abstract Array<WorldObject> getKeepAlives(Level level);
	
	public abstract TileType getTileType(TileTypeEnum type);
	
	public Entity getEntity(int eid) { return entityIDMap.get(eid); }
	
	public Level getLevel(int depth) { return levels.get(depth); }
	
	public Level getEntityLevel(Entity e) { return entityLevels.get(e); }
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	public float getGameTime() { return gameTime; }
	public float getDaylightOffset() { return daylightOffset; }
	
	public String getTimeString() { return TimeOfDay.getTimeString(daylightOffset); }
}
