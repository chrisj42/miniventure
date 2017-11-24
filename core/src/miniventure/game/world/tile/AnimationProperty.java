package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

// FIXME this class will be used for water and stuff, mainly; except, I'm thinking I'll make a subclass, RandomAnimationProperty, to do that; it will pick from a group of frames randomly, at set intervals. I'm not sure what I'm going to do with this. So I'm just going to leave it alone for now.
public class AnimationProperty implements TileProperty {
	
	private int timeElapsed;
	private final int maxTime;
	
	public AnimationProperty(int frameTime) {
		this.maxTime = frameTime;
	}
	
	public TextureRegion getSprite() {
		return null;
	}
	
	@Override
	public int getDataLength() {
		return 1;
	}
}
