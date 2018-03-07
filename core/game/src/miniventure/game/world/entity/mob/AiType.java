package miniventure.game.world.entity.mob;

import miniventure.game.world.ItemDrop;
import miniventure.game.world.entity.mob.PursuePattern.FollowBehavior;

import org.jetbrains.annotations.Nullable;

public enum AiType {
	
	Pig(8, new WanderingPattern(), HitReaction.FLEE/*, new ItemDrop()*/), // later, drops pork food item.
	
	Cow(12, new WanderingPattern(), null),
	
	Crocodile(18, new WanderingPattern(), HitReaction.CHASE),
	
	Zombie(15, new PursuePattern(FollowBehavior.NEAREST_PLAYER), null);
	
	final int health;
	final MovementPattern defaultPattern;
	final HitReaction onHit;
	final ItemDrop[] deathDrops;
	
	AiType(int health, MovementPattern defaultPattern, @Nullable HitReaction onHit, ItemDrop... deathDrops) {
		this.health = health;
		this.defaultPattern = defaultPattern;
		this.onHit = onHit;
		this.deathDrops = deathDrops;
	}
	
	
	public static final AiType[] values = AiType.values();
}
