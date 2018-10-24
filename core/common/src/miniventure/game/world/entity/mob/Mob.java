package miniventure.game.world.entity.mob;

import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;

public interface Mob extends WorldObject {
	
	// for knockback, the whole process should take about 0.5s. The first half at a constant speed, and the second half can be spend slowing down at a linear pace.
	
	float KNOCKBACK_SPEED = 10; // in tiles / second
	float MIN_KNOCKBACK_TIME = 0.05f;
	float MAX_KNOCKBACK_TIME = 0.25f;
	float DAMAGE_PERCENT_FOR_MAX_PUSH = 0.2f;
	
	static float shortenSprite(float height) { return height / 2; }
	
	Direction getDirection();
	boolean isKnockedBack();
	
	static float getKnockbackDuration(float healthPercent) { return MyUtils.mapFloat(Math.min(healthPercent, DAMAGE_PERCENT_FOR_MAX_PUSH), 0, DAMAGE_PERCENT_FOR_MAX_PUSH, MIN_KNOCKBACK_TIME, MAX_KNOCKBACK_TIME); }
}
