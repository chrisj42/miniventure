package miniventure.game.world;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import miniventure.game.util.function.Action;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.WorldConfig;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

/**
 * this class contains all the levels in the game, and generally manages world-level data.
 */
public abstract class WorldManager {
	
	protected static final int INITIAL_ENTITY_BUFFER = 128;
	
	protected float gameTime, daylightOffset;
	private final Map<Integer, Entity> entityIDMap = Collections.synchronizedMap(new HashMap<>(INITIAL_ENTITY_BUFFER));
	
	
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
	
	/** unload and close the world */
	public abstract void exitWorld();
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	protected void clearWorld() { actOnEntityMap(Map::clear); }
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	private void actOnEntityMap(ValueFunction<Map<Integer, Entity>> action) {
		synchronized (entityIDMap) { action.act(entityIDMap); }
	}
	private <T> T getFromEntityMap(MapFunction<Map<Integer, Entity>, T> getter) {
		synchronized (entityIDMap) { return getter.get(entityIDMap); }
	}
	
	public int getEntityTotal() { return getFromEntityMap(Map::size); }
	
	public Entity[] getAllEntities() { return getFromEntityMap(map -> map.values().toArray(new Entity[0])); }
	
	boolean isEntityRegistered(Entity e) { return getFromEntityMap(map -> map.containsKey(e.getId())); }
	
	public void registerEntity(Entity e) { actOnEntityMap(map -> map.put(e.getId(), e)); }
	
	/**
	 * generates a entity id that is unique for this game.
	 * Negative numbers represent entities updated only by the client.
	 */
	public int registerEntityWithNewId(@NotNull Entity e, boolean negative) {
		while(true) {
			final int eid = Math.abs(MathUtils.random.nextInt()) * (negative ? -1 : 1);
			if(eid == 0) continue;
			
			// if put returns null, then eid is new, and therefore valid and should be returned.
			boolean valid = getFromEntityMap(map -> map.putIfAbsent(eid, e)) == null;
			if(valid)
				return eid;
		}
	}
	
	public void deregisterEntity(int eid) { actOnEntityMap(map -> map.remove(eid)); }
	
	
	/*  --- GET METHODS --- */
	
	
	public abstract TileType getTileType(TileTypeEnum type);
	
	public Entity getEntity(int eid) { return getFromEntityMap(map -> map.get(eid)); }
	
	public abstract Level getLevel(int levelId);
	
	public abstract Level getEntityLevel(Entity e);
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	public float getGameTime() { return gameTime; }
	public float getDaylightOffset() { return daylightOffset; }
	
	public String getTimeString() { return TimeOfDay.getTimeString(daylightOffset); }
}
