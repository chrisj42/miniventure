package miniventure.game.world.entitynew.property;

import com.badlogic.gdx.math.Vector2;

@FunctionalInterface
public interface SizeProperty extends EntityProperty {
	Vector2 getSize();
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return SizeProperty.class; }
}
