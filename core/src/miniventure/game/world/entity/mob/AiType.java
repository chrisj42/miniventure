package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.PursuePattern.FollowBehavior;

import org.jetbrains.annotations.Nullable;

public enum AiType {
	
	Pig(8, new WanderingPattern(), HitReaction.FLEE/*, new ItemDrop()*/), // later, drops pork food item.
	
	Cow(12, new WanderingPattern(), null),
	
	Crocodile(18, new WanderingPattern(), HitReaction.CHASE),
	
	Zombie(15, new PursuePattern(FollowBehavior.NEAREST_PLAYER), null);
	
	private final int health;
	private final MovementPattern defaultPattern;
	private final HitReaction onHit;
	private final ItemDrop[] deathDrops;
	
	AiType(int health, MovementPattern defaultPattern, @Nullable HitReaction onHit, ItemDrop... deathDrops) {
		this.health = health;
		this.defaultPattern = defaultPattern;
		this.onHit = onHit;
		this.deathDrops = deathDrops;
	}
	
	public MobAi makeMob() {
		return new MobAi(name().toLowerCase(), health, defaultPattern, deathDrops) {
			@Override
			public boolean attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) {
				if(onHit != null) onHit.onHit(this, obj, attackItem);
				return super.attackedBy(obj, attackItem, damage);
			}
		};
	}
	
	
	public static final AiType[] values = AiType.values();
}
