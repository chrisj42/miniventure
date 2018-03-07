package miniventure.game.world.entitynew;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

public class RenderSetProperty implements RenderProperty {
	
	@FunctionalInterface
	interface AnimationChooser {
		@NotNull RenderProperty getAnimation(Entity e);
	}
	
	private final AnimationChooser animationChooser;
	
	public RenderSetProperty(AnimationChooser chooser) {
		animationChooser = chooser;
	}
	
	
	@Override
	public void render(Entity e, SpriteBatch batch, float x, float y) {
		animationChooser.getAnimation(e).render(e, batch, x, y);
	}
	
}
