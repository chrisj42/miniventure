package miniventure.game.world.entitynew.property;

import miniventure.game.world.entitynew.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

public class RenderSetProperty extends RenderProperty {
	
	@FunctionalInterface
	interface AnimationChooser {
		@NotNull RenderProperty getAnimation(Entity e);
	}
	
	private final AnimationChooser animationChooser;
	
	public RenderSetProperty(AnimationChooser chooser) {
		animationChooser = chooser;
	}
	
	
	@Override
	public void render(Entity e, float delta, SpriteBatch batch, float x, float y) {
		animationChooser.getAnimation(e).render(e, delta, batch, x, y);
	}
	
}
