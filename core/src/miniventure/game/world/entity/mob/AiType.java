package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.entity.mob.PursuePattern.EntityFollower;

import org.jetbrains.annotations.Nullable;

public enum AiType {
	
	Pig(8, new WanderingPattern(), HitReaction.FLEE/*, new ItemDrop()*/), // later, drops pork food item.
	
	Cow(12, new WanderingPattern(), null),
	
	CROCODILE(18, new WanderingPattern(), HitReaction.CHASE),
	
	Zombie(15, new PursuePattern(EntityFollower.NEAREST_PLAYER), null);
	
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
			public boolean attackedBy(Mob mob, Item attackItem) {
				if(onHit != null) onHit.onHit(this, mob, attackItem);
				return super.attackedBy(mob, attackItem);
			}
		};
	}
	
	
	public static final AiType[] values = AiType.values();
}
