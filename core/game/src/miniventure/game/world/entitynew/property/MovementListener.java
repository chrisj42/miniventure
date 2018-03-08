package miniventure.game.world.entitynew.property;

import miniventure.game.world.entitynew.Entity;

import com.badlogic.gdx.math.Vector2;

@FunctionalInterface
public interface MovementListener extends EntityProperty {
	
	void entityMoved(Entity e, Vector2 delta);
	
	default MovementListener combineProperty(MovementListener other) {
		return (e, delta) -> {
			other.entityMoved(e, delta);
			entityMoved(e, delta);
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return MovementListener.class; }
}
