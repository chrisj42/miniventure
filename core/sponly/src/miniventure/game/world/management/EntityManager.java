package miniventure.game.world.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.world.entity.Entity;

class EntityManager {
	
	private final HashMap<Level, Set<Entity>> levelEntities;
	private final HashMap<Entity, Level> entityLevels;
	
	EntityManager() {
		levelEntities = new HashMap<>();
		entityLevels = new HashMap<>();
	}
	
	void clear() {
		levelEntities.clear();
		entityLevels.clear();
	}
	
	private Set<Entity> entitySet(Level level) {
		return levelEntities.getOrDefault(level, Collections.emptySet());
	}
	
	synchronized void addEntity(Entity e, Level level) {
		entityLevels.put(e, level);
		levelEntities.computeIfAbsent(level, k -> new HashSet<>()).add(e);
	}
	
	synchronized void removeEntity(Entity e) {
		entitySet(entityLevels.remove(e)).remove(e);
	}
	
	// returns the entities that were in the level
	synchronized Set<Entity> removeLevel(Level level) {
		Set<Entity> entities = levelEntities.remove(level);
		if(entities == null) return Collections.emptySet();
		
		for(Entity e: entities)
			entityLevels.remove(e);
		
		return entities;
	}
	
	synchronized Level getLevel(Entity entity) {
		return entityLevels.get(entity);
	}
	
	synchronized int getEntityCount() {
		return entityLevels.size();
	}
	
	synchronized int getEntityCount(Level level) {
		return entitySet(level).size();
	}
	
	synchronized HashSet<Entity> getEntities(Level level) {
		return new HashSet<>(entitySet(level));
	}
}
