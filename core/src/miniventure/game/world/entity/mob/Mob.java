package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

/**
 * This class is necessary because it ought to nicely package up the functionality of a mob, that moves around, and has up/down/left/right walking animations. Though, I may move the directional + state driven animation to its own class...
 */
public abstract class Mob extends Entity {
	
	@NotNull private Direction dir;
	@NotNull private MobAnimationController animator;
	
	public Mob(@NotNull String spriteName) {
		super(new Sprite());
		dir = Direction.DOWN;
		
		animator = new MobAnimationController(this, spriteName);
	}
	
	public Direction getDirection() { return dir; }
	
	public void render(SpriteBatch batch, float delta) {
		setSprite(animator.pollAnimation(delta));
		super.render(batch, delta);
	}
	
	@Override
	public void move(float xd, float yd) {
		super.move(xd, yd);
		
		if(xd != 0 || yd != 0) animator.requestState(AnimationState.WALK);
		
		Direction dir = Direction.getDirection(xd, yd);
		if(dir != null) {
			// change sprite direction
			this.dir = dir;
		}
	}
	
	@Override
	public boolean hurtBy(Mob mob, Item attackItem, int dmg) { return true; } // mobs are hurt
}
