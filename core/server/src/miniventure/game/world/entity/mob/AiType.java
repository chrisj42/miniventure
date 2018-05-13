package miniventure.game.world.entity.mob;

import miniventure.game.world.ItemDrop;
import miniventure.game.world.entity.mob.PursuePattern.FollowBehavior;

import org.jetbrains.annotations.Nullable;

public enum AiType {
	
	Pig(8, true, new WanderingPattern(), HitReaction.FLEE, null/*, new ItemDrop()*/), // later, drops pork food item.
	
	Cow(12, true, new WanderingPattern(), null, null),
	
	Crocodile(18, true, new WanderingPattern(), HitReaction.CHASE, TouchReaction.attackPlayer(2)),
	
	Zombie(15, false, new PursuePattern(FollowBehavior.NEAREST_PLAYER), null, TouchReaction.attackPlayer(1));
	
	final int health;
	final boolean daySpawn;
	final MovementPattern defaultPattern;
	final HitReaction onHit;
	final TouchReaction onTouch;
	final ItemDrop[] deathDrops;
	
	AiType(int health, boolean daySpawn, MovementPattern defaultPattern, @Nullable HitReaction onHit, @Nullable TouchReaction onTouch, ItemDrop... deathDrops) {
		this.health = health;
		this.daySpawn = daySpawn;
		this.defaultPattern = defaultPattern;
		this.onHit = onHit;
		this.onTouch = onTouch;
		this.deathDrops = deathDrops;
	}
	
	
	public static final AiType[] values = AiType.values();
}
