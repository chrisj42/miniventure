package miniventure.game.world.entity.mob;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.item.Item;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.entity.Entity;
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
	
	@NotNull private AiType aiType;
	
	@NotNull private ItemDrop[] itemDrops;
	
	@NotNull private MovementPattern movePattern;
	@Nullable private MovementPattern tempMovePattern = null;
	private float tempTimeLeft = 0;
	
	protected MobAi(@NotNull AiType aiType) {
		super(aiType.name().toLowerCase(), aiType.health);
		this.aiType = aiType;
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	// if a subclass was made of this, then it may not save the ai type.
	protected MobAi(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(2);
		aiType = AiType.valueOf(data.get(0));
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			aiType.name()
		));
		
		allData.add(data);
		return allData;
	}
	
	public AiType getType() { return aiType; }
	
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
	public boolean attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) {
		if(aiType.onHit != null) aiType.onHit.onHit(this, obj, attackItem);
		return super.attackedBy(obj, attackItem, damage);
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(aiType.onTouch != null) return aiType.onTouch.onTouch(this, other);
		return super.touchedBy(other);
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
	public boolean maySpawn() {
		return super.maySpawn() && aiType.spawnBehavior.maySpawn(this);
	}
	
	@Override
	public boolean maySpawn(TileTypeEnum type) {
		if(!aiType.spawnBehavior.hasTiles())
			return super.maySpawn(type);
		return aiType.spawnBehavior.maySpawn(type);
	}
}
