package miniventure.game.world.entity.particle;

import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.Nullable;

public class BounceBehavior {
	
	private static final float GRAVITY = -50;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private Vector3 velocity; // in tiles / second.
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	private float lastBounceTime; // used to halt the entity once it starts bouncing a lot really quickly.
	
	public BounceBehavior(@Nullable Vector2 goalDir) {
		if(goalDir == null)
			goalDir = new Vector2().setToRandomDirection();
		
		velocity = new Vector3(goalDir, MathUtils.random(8f, 12f));
	}
	
	BounceBehavior(String[] data) {
		float x = Float.parseFloat(data[0]);
		float y = Float.parseFloat(data[1]);
		float z = Float.parseFloat(data[2]);
		velocity = new Vector3(x, y, z);
		time = Float.parseFloat(data[3]);
		lastBounceTime = Float.parseFloat(data[4]);
	}
	
	String[] save() {
		return new String[] {
			String.valueOf(velocity.x),
			String.valueOf(velocity.y),
			String.valueOf(velocity.z),
			String.valueOf(time),
			String.valueOf(lastBounceTime)
		};
	}
	
	void scaleVelocity(float amt) { velocity.scl(amt, amt, 1); }
	
	float getTime() { return time; }
	
	private boolean moving = true;
	public void update(Entity e, float delta) {
		/*
			Movement will work like this:
				- the itemEntity will move along a base axis, as time progresses, and the actual position will vary according to time.
				- b/c we don't know the current position, we will just have to assume the starting point is (0,0), and move the delta dist for this frame, based on the passed in delta and the total move time.
		 */
		
		Level level = e.getLevel();
		if(level == null) return;
		
		Vector2 pos = e.getPosition();
		Vector3 vel = velocity.cpy().scl(delta);
		e.move(vel.x, vel.y, vel.z);
		Vector2 newPos = e.getPosition();
		
		if(newPos.x != pos.x+vel.x)
			velocity.x *= -1;
		if(newPos.y != pos.y+vel.y)
			velocity.y *= -1;
		
		time += delta;
		
		if(e.getZ() < 0) {
			e.move(0, 0, -e.getZ());
			velocity.scl(REBOUND_SPEED_FACTOR, REBOUND_SPEED_FACTOR, -REBOUND_SPEED_FACTOR);
			if(time - lastBounceTime < 0.01f) {
				moving = false;
				velocity.setZero();
			}
			else
				lastBounceTime = time;
		}
		
		if(moving)
			velocity.add(0, 0, GRAVITY*delta);
	}
}
