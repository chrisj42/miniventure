package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.management.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobAi extends Mob {
	
	/*
		The key component of MobAis is just that they will move by themselves.
		There are a couple different move patterns they could follow, however.
			Perhaps this would best be represented with an enum? An enum of MovementPatterns.
			We also need a way to determine which movement pattern to use at any given time, and switch between patterns.
			
			Here's a few standard patterns:
				- wandering. every so often, the mob picks a random direction, and walks for some duration.
				- follow player. pretty simple, the mob follows the player. Perhaps as long as the player is within a certain range.
				- run from player. reverse of above.
				
			and... that's about it, really...
	 */
	
	@NotNull private AiType aiType;
	
	@NotNull private ItemDrop[] itemDrops;
	
	@NotNull private MovementPattern movePattern;
	@Nullable private MovementPattern tempMovePattern = null;
	private float tempTimeLeft = 0;
	
	private int health;
	
	protected MobAi(@NotNull EntitySpawn info, @NotNull AiType aiType) {
		super(info, aiType.name().toLowerCase());
		this.aiType = aiType;
		this.health = aiType.health;
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	// if a subclass was made of this, then it may not save the ai type.
	protected MobAi(@NotNull Level level, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(level, allData, version, modifier);
		
		SerialHashMap data = allData.get("ai");
		aiType = data.get("type", AiType::valueOf);
		
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		
		SerialHashMap data = new SerialHashMap();
		data.add("type", aiType);
		
		allData.put("ai", data);
		return allData;
	}
	
	@Override
	public void reset() {
		super.reset();
		health = aiType.health;
		tempMovePattern = null;
		tempTimeLeft = 0;
		movePattern = aiType.defaultPattern.copy();
	}
	
	public AiType getType() { return aiType; }
	
	@Override
	protected int getMaxHealth() { return aiType.health; }
	@Override
	protected int getHealth() { return health; }
	@Override
	protected int changeHealth(int amount) {
		final int prev = health;
		health = MyUtils.clamp(health + amount, 0, getMaxHealth());
		return health - prev;
	}
	
	//protected void setMovePattern(@NotNull MovementPattern pattern) { movePattern = pattern; }
	protected void setMovePattern(@NotNull MovementPattern pattern, float duration) {
		tempMovePattern = pattern;
		tempTimeLeft = duration;
	}
	MovementPattern curMovePattern() { return tempMovePattern == null ? movePattern : tempMovePattern; }
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		if(tempTimeLeft > 0) tempTimeLeft -= delta;
		
		if(tempTimeLeft <= 0 && tempMovePattern != null)
			tempMovePattern = null;
		
		move(curMovePattern().move(delta, this));
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) {
		if(aiType.onHit != null) aiType.onHit.onHit(this, obj, (Item)attackItem);
		return super.attackedBy(obj, attackItem, damage);
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(aiType.onTouch != null) return aiType.onTouch.onTouch(this, other);
		return super.touchedBy(other);
	}
	
	@Override
	public void die() {
		for (ItemDrop drop: itemDrops)
			getLevel().dropItems(drop, this, null);	
		
		super.die();
	}
	
	@Override
	public String toString() {
		return super.toString()+'('+aiType+')';
	}
}
