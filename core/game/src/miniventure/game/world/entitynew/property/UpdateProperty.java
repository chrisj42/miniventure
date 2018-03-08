package miniventure.game.world.entitynew.property;

import miniventure.game.api.Combinable;
import miniventure.game.world.entitynew.Entity;

@FunctionalInterface
public interface UpdateProperty extends EntityProperty, Combinable<UpdateProperty> {
	
	void update(Entity e, float delta);
	
	@Override
	default UpdateProperty combine(UpdateProperty other) {
		UpdateProperty update = this; // just in case...
		return (e, delta) -> {
			other.update(e, delta);
			update.update(e, delta);
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return UpdateProperty.class; }
}
