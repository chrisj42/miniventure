package miniventure.game.world.entitynew.property;

import miniventure.game.world.entitynew.Entity;

public interface PermeableProperty extends EntityProperty {
	
	PermeableProperty DEFAULT = (e, o, delegate) -> {
		if(delegate)
			return o.getType().getProp(PermeableProperty.class).isPermeable(o, e, false);
		return false;
	};
	
	PermeableProperty SOLID = (e, o, delegate) -> false;
	PermeableProperty NONSOLID = (e, o, delegate) -> true;
	
	boolean isPermeable(Entity entity, Entity other, boolean delegate);
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return PermeableProperty.class; }
}
