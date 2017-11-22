package miniventure.game.world.entity.mob;

import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

/**
 * This class is necessary because it ought to nicely package up the functionality of a mob, that moves around, and has up/down/left/right walking animations. Though, I may move the "directional + state driven animation to its own class...
 */
public class Mob extends Entity {
	
	@NotNull private Direction dir;
	@NotNull private MobAnimationController animator;
	@NotNull private String spriteName;
	
	public Mob(@NotNull String spriteName) {
		dir = Direction.DOWN;
		this.spriteName = spriteName;
		
		animator = new MobAnimationController(this);
	}
	
	public Direction getDirection() { return dir; }
	
	@NotNull
	public String getSpriteName() {
		return spriteName;
	}
	
	public void dispose() {
		animator.dispose();
	}
	
	public void render(SpriteBatch batch, float delta) {
		animator.update(delta);
		
		TextureRegion r = animator.getFrame();
		batch.draw(r, x-r.getRegionWidth()/2, y-r.getRegionHeight()/2);
	}
	
	@Override
	public void move(int xd, int yd) {
		super.move(xd, yd);
		
		if(xd != 0 || yd != 0) animator.requestState(AnimationState.WALK);
		
		Direction dir = Direction.getDirection(xd, yd);
		if(dir != null) {
			// change sprite direction
			this.dir = dir;
		}
	}
}
