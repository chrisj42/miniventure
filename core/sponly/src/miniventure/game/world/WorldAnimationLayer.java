package miniventure.game.world;

import miniventure.game.texture.TextureHolder;
import miniventure.game.texture.layer.AnimationLayer;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.graphics.g2d.Animation;

// an animation that will be somehow connected to world space, so it makes sense to get the state time from the game time.
public class WorldAnimationLayer extends AnimationLayer {
	
	private final WorldManager world;
	
	public WorldAnimationLayer(WorldManager world, Animation<TextureHolder> animation) {
		super(animation, null);
		this.world = world;
	}
	
	// @Override
	protected float getStateTime() {
		return world.getGameTime();
	}
}
