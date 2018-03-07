package miniventure.game.world.entitynew;

import miniventure.game.api.APIObjectType;
import miniventure.game.api.PropertyFetcher;

import org.jetbrains.annotations.NotNull;

public enum EntityType implements APIObjectType<EntityType, EntityProperty> {
	
	;
	
	private final PropertyFetcher<EntityProperty> propertyFetcher;
	
	EntityType(@NotNull PropertyFetcher<EntityProperty> fetcher) {
		this.propertyFetcher = fetcher;
	}
	
	@Override public EntityProperty[] getProperties() { return propertyFetcher.getProperties(); }
	
	@Override public Class<EntityType> getTypeClass() { return EntityType.class; }
	@Override public EntityType getInstance() { return this; }
	
	public static final EntityType[] values = EntityType.values();
}
