package miniventure.game.world.entity;

import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.Boundable;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class KnockbackController {
	
	@NotNull private final Entity entity;
	
	// knockback is applied once, at the start, as a velocity. The mob is moved with this velocity constantly, until the knockback is gone.
	private Vector2 knockbackVelocity = VectorPool.POOL.obtain(0, 0);
	private float knockbackTimeRemaining = 0;
	
	public KnockbackController(@NotNull Entity target) {
		entity = target;
	}
	
	public void knock(Boundable knocker, float speed, float duration) {
		Vector2 center = knocker.getCenter();
		Vector2 knockDir = entity.getCenter().sub(center).nor();
		knock(knockDir, speed, duration);
		VectorPool.POOL.free(center);
		VectorPool.POOL.free(knockDir);
	}
	public void knock(Vector2 dir, float speed, float duration) {
		// null check in case this is the frame of removal and it got removed before getting updated
		if(knockbackVelocity != null) knockbackVelocity.set(dir).nor().scl(speed);
		knockbackTimeRemaining = duration;
	}
	
	public void update(float delta) {
		if(hasKnockback()) {
			knockbackTimeRemaining -= delta;
			entity.move(VectorPool.POOL.obtain(knockbackVelocity).scl(delta), true);
			
			if(knockbackTimeRemaining <= 0)
				reset();
		}
	}
	
	public boolean hasKnockback() {
		return knockbackTimeRemaining > 0 && knockbackVelocity != null && !knockbackVelocity.isZero();
	}
	
	public void reset() {
		knockbackTimeRemaining = 0;
		if(knockbackVelocity != null) knockbackVelocity.setZero();
	}
	
	public void free() {
		VectorPool.POOL.free(knockbackVelocity);
		knockbackVelocity = null;
	}
}
