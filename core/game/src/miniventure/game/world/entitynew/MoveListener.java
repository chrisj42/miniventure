package miniventure.game.world.entitynew;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
public interface MoveListener extends EntityProperty {
	
	void entityMoved(Vector3 amount, Entity e);
	
}
