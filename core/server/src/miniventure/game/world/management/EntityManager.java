package miniventure.game.world.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.level.ServerLevel;

class EntityManager {
	
	private final HashMap<ServerLevel, Set<ServerEntity>> levelEntities;
	private final HashMap<ServerEntity, ServerLevel> entityLevels;
	
	EntityManager() {
		levelEntities = new HashMap<>();
		entityLevels = new HashMap<>();
	}
	
	void clear() {
		levelEntities.clear();
		entityLevels.clear();
	}
	
	private Set<ServerEntity> entitySet(ServerLevel level) {
		return levelEntities.getOrDefault(level, Collections.emptySet());
	}
	
	synchronized void addEntity(ServerEntity e, ServerLevel level) {
		entityLevels.put(e, level);
		levelEntities.computeIfAbsent(level, k -> new HashSet<>()).add(e);
	}
	
	synchronized void removeEntity(ServerEntity e) {
		entitySet(entityLevels.remove(e)).remove(e);
	}
	
	// returns the entities that were in the level
	synchronized Set<ServerEntity> removeLevel(ServerLevel level) {
		Set<ServerEntity> entities = levelEntities.remove(level);
		if(entities == null) return Collections.emptySet();
		
		for(ServerEntity e: entities)
			entityLevels.remove(e);
		
		return entities;
	}
	
	synchronized ServerLevel getLevel(ServerEntity entity) {
		return entityLevels.get(entity);
	}
	
	synchronized int getEntityCount() {
		return entityLevels.size();
	}
	
	synchronized int getEntityCount(ServerLevel level) {
		return entitySet(level).size();
	}
	
	synchronized HashSet<ServerEntity> getEntities(ServerLevel level) {
		return new HashSet<>(entitySet(level));
	}
}
