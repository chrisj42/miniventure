package miniventure.game.world.entity.mob;

import java.util.Arrays;

import miniventure.game.item.Item;
import miniventure.game.util.Version;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;

import com.badlogic.gdx.utils.Array;

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
	
	public MobAi(@NotNull AiType aiType) {
		super("mob", aiType.health);
		this.aiType = aiType;
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	protected MobAi(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		aiType = AiType.valueOf(data[0]);
		this.itemDrops = aiType.deathDrops;
		this.movePattern = aiType.defaultPattern.copy();
	}
	
	public MobAi(MobAi model) { this(model.aiType); }
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			aiType.name()
		});
		
		return data;
	}
	
	protected AiType getType() { return aiType; }
	
	protected void setMovePattern(@NotNull MovementPattern pattern) { movePattern = pattern; }
	protected void setMovePattern(@NotNull MovementPattern pattern, float duration) {
		tempMovePattern = pattern;
		tempTimeLeft = duration;
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		if(tempTimeLeft > 0) tempTimeLeft -= delta;
		
		if(tempTimeLeft <= 0 && tempMovePattern != null)
			tempMovePattern = null;
		
		move((tempMovePattern == null ? movePattern : tempMovePattern).move(delta, this));
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) {
		if(aiType.onHit != null) aiType.onHit.onHit(this, obj, attackItem);
		return super.attackedBy(obj, attackItem, damage);
	}
	
	@Override
	public void die() {
		ServerLevel level = getLevel();
		if(level != null)
			for (ItemDrop drop: itemDrops)
				level.dropItems(drop, this, null);
		
		super.die();
	}
}
