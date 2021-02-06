package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobAi extends ServerMob {
	
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
	
	@NotNull private final AiType aiType;
	
	@NotNull private final ItemDrop[] itemDrops;
	
	@NotNull private final MovementPattern movePattern;
	@Nullable private MovementPattern tempMovePattern = null;
	private float tempTimeLeft = 0;
	
	protected MobAi(@NotNull ServerWorld world, @NotNull AiType aiType) {
		super(world, aiType.name().toLowerCase(), aiType.health);
		this.aiType = aiType;
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	// if a subclass was made of this, then it may not save the ai type.
	protected MobAi(@NotNull ServerWorld world, EntityDataSet data, final Version version, ValueAction<EntityDataSet> modifier) {
		super(world, data, version, d -> {
			modifier.act(d);
			d.setPrefix("ai");
			AiType type = d.get("type", AiType::valueOf);
			d.setPrefix("mob");
			d.add("sprite", type.name().toLowerCase());
			d.add("mhp", type.health);
		});
		
		data.setPrefix("ai");
		aiType = data.get("type", AiType::valueOf);
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet data = super.save();
		
		data.setPrefix("mob");
		data.remove("sprite");
		data.remove("mhp");
		
		data.setPrefix("ai");
		data.add("type", aiType);
		
		return data;
	}
	
	public AiType getType() { return aiType; }
	
	//protected void setMovePattern(@NotNull MovementPattern pattern) { movePattern = pattern; }
	protected void setMovePattern(@NotNull MovementPattern pattern, float duration) {
		if(tempMovePattern != null) tempMovePattern.free();
		tempMovePattern = pattern;
		tempTimeLeft = duration;
	}
	MovementPattern curMovePattern() { return tempMovePattern == null ? movePattern : tempMovePattern; }
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		if(tempTimeLeft > 0) tempTimeLeft -= delta;
		
		if(tempTimeLeft <= 0 && tempMovePattern != null) {
			tempMovePattern.free();
			tempMovePattern = null;
		}
		
		move(curMovePattern().move(delta, this, VectorPool.POOL.obtain()), true);
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) {
		if(aiType.onHit != null) aiType.onHit.onHit(this, obj, (ServerItem)attackItem);
		return super.attackedBy(obj, attackItem, damage);
	}
	
	@Override
	public void touchedBy(Entity other, boolean initial) {
		if(aiType.onTouch != null)
			aiType.onTouch.onTouch(this, other);
	}
	
	@Override
	public void die() {
		ServerLevel level = getLevel();
		if(level != null)
			for (ItemDrop drop: itemDrops)
				level.dropItems(drop, this, null);
		
		super.die();
	}
	
	@Override
	public void remove() {
		super.remove();
		movePattern.free();
		if(tempMovePattern != null) tempMovePattern.free();
	}
	
	@Override
	public boolean maySpawn() {
		return super.maySpawn() && aiType.spawnBehavior.maySpawn(this);
	}
	
	@Override
	public boolean maySpawn(TileTypeEnum type) {
		if(!aiType.spawnBehavior.hasTiles())
			return super.maySpawn(type);
		return aiType.spawnBehavior.maySpawn(type);
	}
	
	@Override
	public String toString() {
		return super.toString()+'('+aiType+')';
	}
}
