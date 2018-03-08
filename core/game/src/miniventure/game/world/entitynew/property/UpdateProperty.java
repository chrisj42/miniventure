package miniventure.game.world.entitynew.property;

import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface UpdateProperty extends EntityProperty {
	
	void update(Entity e, float delta);
	
	default UpdateProperty combineProperty(UpdateProperty other) {
		return (e, delta) -> {
			other.update(e, delta);
			UpdateProperty.this.update(e, delta);
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return UpdateProperty.class; }
}
