package miniventure.game.world.entity;

import miniventure.game.world.WorldObject;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class KnockbackController {
	
	@NotNull private final Entity entity;
	
	// knockback is applied once, at the start, as a velocity. The mob is moved with this velocity constantly, until the knockback is gone.
	private Vector2 knockbackVelocity = new Vector2();
	private float knockbackTimeRemaining = 0;
	
	public KnockbackController(@NotNull Entity target) {
		entity = target;
	}
	
	public void knock(WorldObject knocker, float speed, float duration) {
		Vector2 knockDir = entity.getCenter().sub(knocker.getCenter()).nor();
		knock(knockDir, speed, duration);
	}
	public void knock(Vector2 dir, float speed, float duration) {
		knockbackVelocity.set(dir).nor().scl(speed);
		knockbackTimeRemaining = duration;
	}
	
	public void update(float delta) {
		if(hasKnockback()) {
			knockbackTimeRemaining -= delta;
			entity.move(knockbackVelocity.cpy().scl(delta));
			
			if(knockbackTimeRemaining <= 0)
				reset();
		}
	}
	
	public boolean hasKnockback() { return knockbackTimeRemaining > 0 && !knockbackVelocity.isZero(); }
	
	public void reset() {
		knockbackTimeRemaining = 0;
		knockbackVelocity.setZero();
	}
}
