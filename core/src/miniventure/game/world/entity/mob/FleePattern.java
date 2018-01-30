package miniventure.game.world.entity.mob;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class FleePattern extends PursuePattern {
	public FleePattern() { super(); }
	public FleePattern(EntityFollower followBehavior) { super(followBehavior); }
	public FleePattern(@NotNull EntityFollower followBehavior, float maxDist, float followSpeed) {
		super(followBehavior, maxDist, followSpeed);
	}
	
	@Override
	public Vector2 move(float delta, MobAi mob) {
		Vector2 vector = super.move(delta, mob);
		vector.rotate(180); // this will technically affect vectors from the idle wandering pattern, but it doesn't really matter, since the direction is random anyway.
		return vector;
	}
}
