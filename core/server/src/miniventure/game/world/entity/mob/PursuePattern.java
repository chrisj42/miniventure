package miniventure.game.world.entity.mob;

import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.level.Level;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PursuePattern implements MovementPattern {
	
	@FunctionalInterface
	interface FollowBehavior {
		@Nullable WorldObject getObjectToFollow(MobAi self);
		
		FollowBehavior NEAREST_PLAYER = (self) -> {
			Level level = self.getLevel();
			if(level == null) return null;
			
			return level.getClosestPlayer(self.getCenter(), true);
		};
	}
	
	@NotNull private final FollowBehavior followBehavior;
	private final float maxDist;
	private final float followSpeed;
	
	private final WanderingPattern idlePattern;
	private boolean wasFollowing = false;
	
	public PursuePattern() { this(FollowBehavior.NEAREST_PLAYER); }
	public PursuePattern(FollowBehavior followBehavior) { this(followBehavior, 2.5f); }
	public PursuePattern(FollowBehavior followBehavior, float followSpeed) { this(followBehavior, 8, followSpeed); }
	public PursuePattern(@NotNull FollowBehavior behavior, float maxDist, float followSpeed) {
		this.followBehavior = behavior;
		this.maxDist = maxDist;
		this.followSpeed = followSpeed;
		
		idlePattern = new WanderingPattern();
	}
	
	@Override
	public Vector2 move(float delta, MobAi mob, Vector2 movement) {
		WorldObject follow = followBehavior.getObjectToFollow(mob);
		if(follow == null) return VectorPool.POOL.obtain(0, 0);
		
		Vector2 followCenter = follow.getCenter();
		Vector2 mobCenter = mob.getCenter();
		followCenter.sub(mobCenter);
		movement.set(followCenter);
		VectorPool.POOL.free(followCenter);
		VectorPool.POOL.free(mobCenter);
		
		if(maxDist <= 0 || followCenter.len() < maxDist) { // move toward the entity
			movement.setLength(followSpeed * delta);
			wasFollowing = true;
		} else {
			if(wasFollowing) idlePattern.reset();
			wasFollowing = false;
			idlePattern.move(delta, mob, movement);
		}
		
		return movement;
	}
	
	@Override
	public void free() {
		idlePattern.free();
	}
	
	@Override
	public MovementPattern copy() {
		return new PursuePattern(followBehavior, maxDist, followSpeed);
	}
	
	public static class FleePattern extends PursuePattern {
		
		public FleePattern() { super(); }
		public FleePattern(FollowBehavior followBehavior) { super(followBehavior); }
		public FleePattern(FollowBehavior followBehavior, float followSpeed) { super(followBehavior, followSpeed); }
		public FleePattern(@NotNull FollowBehavior followBehavior, float maxDist, float followSpeed) {
			super(followBehavior, maxDist, followSpeed);
		}
		
		@Override
		public Vector2 move(float delta, MobAi mob, Vector2 movement) {
			Vector2 vector = super.move(delta, mob, movement);
			vector.rotate(180); // this will technically affect vectors from the idle wandering pattern, but it doesn't really matter, since the direction is random anyway.
			return vector;
		}
		
		@Override
		public MovementPattern copy() {
			return new FleePattern(super.followBehavior, super.maxDist, super.followSpeed);
		}
	}
}
