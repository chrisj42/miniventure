package miniventure.game.world.entity.mob;

import com.badlogic.gdx.math.Vector2;

@FunctionalInterface
public interface MovementPattern {
	
	Vector2 move(float delta, MobAi mob);
	
}
