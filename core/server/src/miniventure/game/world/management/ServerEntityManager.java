package miniventure.game.world.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.level.ServerLevel;

class ServerEntityManager {
	
	private final HashMap<ServerLevel, Set<ServerEntity>> levelEntities;
	private final HashMap<ServerEntity, ServerLevel> entityLevels;
	
	ServerEntityManager() {
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
	
	private final ArrayList<ServerEntity> tempEntities = new ArrayList<>(80);
	synchronized void forEachEntity(ServerLevel level, ValueAction<ServerEntity> action) {
		final boolean nested = tempEntities.size() > 0;
		if(!nested)
			tempEntities.addAll(entitySet(level));
		for(ServerEntity e: tempEntities)
			action.act(e);
		if(!nested)
			tempEntities.clear();
	}
}
