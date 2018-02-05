package miniventure.game.world.entity.mob;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class WanderingPattern implements MovementPattern {
	
	private static final float MIN_PAUSE_TIME = 1f;
	private static final float MAX_PAUSE_TIME = 10f;
	private static final float MIN_MOVE_TIME = 0.5f;
	private static final float MAX_MOVE_TIME = 5f;
	
	public static final float MOVEMENT_SPEED = 1.8f; // in tiles/second.
	
	
	private float movementTimeLeft = 0;
	private boolean isStopped = false;
	private Vector2 moveDir = new Vector2();
	
	public WanderingPattern() {
		toggleWalk();
	}
	
	public void reset() {
		toggleWalk();
		if(!isStopped)
			toggleWalk();
	}
	
	@Override
	public Vector2 move(float delta, MobAi mob) {
		movementTimeLeft -= delta;
		if(movementTimeLeft < 0) delta += movementTimeLeft;
		
		Vector2 movement = new Vector2();
		
		if(!isStopped) {
			movement.set(moveDir);
			movement.scl(MOVEMENT_SPEED * delta);
		}
		
		if(movementTimeLeft <= 0)
			toggleWalk();
		
		return movement;
	}
	
	private void toggleWalk() {
		isStopped = !isStopped;
		
		if(isStopped)
			movementTimeLeft = MathUtils.random(MIN_PAUSE_TIME, MAX_PAUSE_TIME);
		else
			movementTimeLeft = MathUtils.random(MIN_MOVE_TIME, MAX_MOVE_TIME);
		
		if(!isStopped)
			moveDir.setToRandomDirection();
	}
}
