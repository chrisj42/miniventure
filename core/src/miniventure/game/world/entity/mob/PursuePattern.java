package miniventure.game.world.entity.mob;

import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PursuePattern implements MovementPattern {
	
	@FunctionalInterface
	interface EntityFollower {
		@Nullable Entity getEntityToFollow(MobAi self);
		
		EntityFollower NEAREST_PLAYER = (self) -> {
			Level level = self.getLevel();
			if(level == null) return null;
			
			return level.getClosestPlayer(self.getCenter());
		};
	}
	
	@NotNull private EntityFollower followBehavior;
	private float maxDist;
	private float followSpeed;
	
	private WanderingPattern idlePattern;
	private boolean wasFollowing = false;
	
	public PursuePattern() { this(EntityFollower.NEAREST_PLAYER); }
	public PursuePattern(EntityFollower followBehavior) { this(followBehavior, 2.5f); }
	public PursuePattern(EntityFollower followBehavior, float followSpeed) { this(followBehavior, 8 * Tile.SIZE, followSpeed); }
	public PursuePattern(@NotNull EntityFollower behavior, float maxDist, float followSpeed) {
		this.followBehavior = behavior;
		this.maxDist = maxDist;
		this.followSpeed = followSpeed;
		
		idlePattern = new WanderingPattern();
	}
	
	@Override
	public Vector2 move(float delta, MobAi mob) {
		Entity follow = followBehavior.getEntityToFollow(mob);
		if(follow == null) return new Vector2();
		
		Vector2 dist = follow.getCenter();
		dist.sub(mob.getCenter());
		
		if(maxDist <= 0 || dist.len() < maxDist) { // move toward the entity
			dist.setLength(followSpeed * delta);
			wasFollowing = true;
		} else {
			if(wasFollowing) idlePattern.reset();
			wasFollowing = false;
			dist.set(idlePattern.move(delta, mob));
		}
		
		return dist;
	}
	
	@Override
	public MovementPattern copy() {
		return new PursuePattern(followBehavior, maxDist, followSpeed);
	}
	
	public static class FleePattern extends PursuePattern {
		
		public FleePattern() { super(); }
		public FleePattern(EntityFollower followBehavior) { super(followBehavior); }
		public FleePattern(EntityFollower followBehavior, float followSpeed) { super(followBehavior, followSpeed); }
		public FleePattern(@NotNull EntityFollower followBehavior, float maxDist, float followSpeed) {
			super(followBehavior, maxDist, followSpeed);
		}
		
		@Override
		public Vector2 move(float delta, MobAi mob) {
			Vector2 vector = super.move(delta, mob);
			vector.rotate(180); // this will technically affect vectors from the idle wandering pattern, but it doesn't really matter, since the direction is random anyway.
			return vector;
		}
		
		@Override
		public MovementPattern copy() {
			return new FleePattern(super.followBehavior, super.maxDist, super.followSpeed);
		}
	}
}
