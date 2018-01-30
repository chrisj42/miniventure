package miniventure.game.world.entity.mob;

import miniventure.game.world.ItemDrop;

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
	
	@NotNull private MovementPattern movePattern;
	@Nullable private MovementPattern tempMovePattern = null;
	private float tempTimeLeft = 0;
	
	public MobAi(@NotNull String spriteName, int health, @NotNull ItemDrop... deathDrops) {
		this(spriteName, health, new WanderingPattern(), deathDrops);
	}
	public MobAi(@NotNull String spriteName, int health, @NotNull MovementPattern movePattern, @NotNull ItemDrop... deathDrops) {
		super(spriteName, health, deathDrops);
		this.movePattern = movePattern;
	}
	
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
	
}
