package miniventure.game.world.entity.mob;

import miniventure.game.item.FoodType;
import miniventure.game.item.ResourceType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.entity.mob.PursuePattern.FollowBehavior;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AiType {
	
	Pig(8, SpawnBehavior.DEFAULT, new WanderingPattern(), HitReaction.FLEE, null,
		new ItemDrop(FoodType.Raw_Bacon.get(), 1, 5, .4f)
	),
	
	Cow(12, SpawnBehavior.DEFAULT, new WanderingPattern(), null, null,
		new ItemDrop(FoodType.Raw_Meat.get(), 2)
	),
	
	Crocodile(14, SpawnBehavior.custom(null, TileTypeEnum.WATER), new WanderingPattern(), HitReaction.CHASE, TouchReaction.attackPlayer(2),
		new ItemDrop(FoodType.Raw_Meat.get(), 1, 2)
	),
	
	Zombie(15, SpawnBehavior.DEFAULT_NIGHT, new PursuePattern(FollowBehavior.NEAREST_PLAYER), null, TouchReaction.attackPlayer(1),
		new ItemDrop(FoodType.Gooseberry.get(), 0, 1, 0.2f),
		new ItemDrop(ResourceType.Fabric.get(), 1, 3, 0.35f),
		new ItemDrop(ResourceType.Cotton.get(), 0, 1, 0.275f)
	);
	
	final int health;
	@NotNull final SpawnBehavior spawnBehavior;
	@NotNull final MovementPattern defaultPattern;
	@Nullable final HitReaction onHit;
	@Nullable final TouchReaction onTouch;
	@NotNull final ItemDrop[] deathDrops;
	
	AiType(int health, @NotNull SpawnBehavior spawnBehavior, @NotNull MovementPattern defaultPattern, @Nullable HitReaction onHit, @Nullable TouchReaction onTouch, @NotNull ItemDrop... deathDrops) {
		this.health = health;
		this.spawnBehavior = spawnBehavior;
		this.defaultPattern = defaultPattern;
		this.onHit = onHit;
		this.onTouch = onTouch;
		this.deathDrops = deathDrops;
	}
	
	public MobAi makeMob() { return new MobAi(this); }
	
	public static final AiType[] values = AiType.values();
}
