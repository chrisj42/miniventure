package miniventure.game.world.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import miniventure.game.core.GameCore;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.math.MathUtils;

/**
 * this class contains all the levels in the game, and generally manages world-level data.
 */
public abstract class WorldManager {
	
	protected float gameTime, daylightOffset;
	
	// registered entities
	private final Map<Integer, Entity> entityIDMap = new HashMap<>(128);
	// entities that have reserved an id but may not yet be fully initialized; they will be registered as soon as they are added to a level.
	private final Set<Integer> reservedIDs = new HashSet<>(16);
	// sync lock for both the id map and set together.
	private final Object idLock = new Object();
	
	
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
	
	
	protected void clearEntityIdMap() {
		synchronized (idLock) {
			entityIDMap.clear();
			reservedIDs.clear();
		}
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	public int getEntityTotal() { synchronized (idLock) { return entityIDMap.size(); } }
	
	boolean isEntityRegistered(Entity e) { return isEntityRegistered(e, false); }
	boolean isEntityRegistered(Entity e, boolean includeReserved) {
		synchronized (idLock) {
			return entityIDMap.containsKey(e.getId()) || (includeReserved && reservedIDs.contains(e.getId()));
		}
	}
	
	public Set<Entity> getRegisteredEntities() { synchronized (idLock) { return new HashSet<>(entityIDMap.values()); } }
	
	// only register entities in the reserved id map; give warning otherwise, or TODO possibly fail (check uses).
	public void registerEntity(Entity e) { registerEntity(e, true); }
	void registerEntity(Entity e, boolean shouldBeReserved) {
		synchronized (idLock) {
			if(!reservedIDs.remove(e.getId()) && shouldBeReserved)
				GameCore.error(this+" has not reserved entity ID "+e.getId()+"; continuing registration of given entity "+e);
			
			// check for existing registration
			Entity cur;
			if((cur = entityIDMap.put(e.getId(), e)) != null)
				GameCore.error(this+" recieved redundant registration request for entity "+e+"; existing mapping (should match): "+cur);
		}
	}
	
	/**
	 * generates a entity id that is unique for this game.
	 * Negative numbers represent entities updated only by the client.
	 */
	public int reserveNewEntityId(boolean negative) {
		while(true) {
			final int eid = Math.abs(MathUtils.random.nextInt()) * (negative ? -1 : 1);
			if(eid == 0) continue;
			
			synchronized (idLock) {
				// if it is not contained in the id map or the reserved set, then its free
				if(!entityIDMap.containsKey(eid) && !reservedIDs.contains(eid)) {
					reservedIDs.add(eid);
					return eid;
				}
			}
		}
	}
	
	public void cancelIdReservation(Entity e) {
		synchronized (idLock) {
			if(!reservedIDs.remove(e.getId()))
				GameCore.error("(in WorldManager.cancelIdReservation): id for entity " + e + " not reserved.");
		}
	}
	
	public void deregisterEntity(int eid) {
		synchronized (idLock) {
			if(entityIDMap.remove(eid) == null)
				GameCore.error("(in WorldManager.deregisterEntity): id "+eid+" not registered.");
		}
	}
	
	
	/*  --- GET METHODS --- */
	
	
	public abstract TileType getTileType(TileTypeEnum type);
	
	public Entity getEntity(int eid) { synchronized (idLock) { return entityIDMap.get(eid); } }
	
	public abstract Level getLevel(int levelId);
	
	public abstract Level getEntityLevel(Entity e);
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	public float getGameTime() { return gameTime; }
	public float getDaylightOffset() { return daylightOffset; }
	
	public String getTimeString() { return TimeOfDay.getTimeString(daylightOffset); }
}
