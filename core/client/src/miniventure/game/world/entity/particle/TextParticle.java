package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.EntityRenderer.TextRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TextParticle extends ClientParticle {
	
	private final BounceBehavior bounceBehavior;
	private final LifetimeTracker lifetime;
	
	public TextParticle(TextParticleData data) {
		lifetime = new LifetimeTracker(this, 2f);
		
		bounceBehavior = new BounceBehavior((Vector2)null);
		bounceBehavior.scaleVelocity(MathUtils.random(0.5f, 2.5f));
		
		String text = data.text;
		Color main = Color.valueOf(data.mainColor);
		Color shadow = Color.valueOf(data.shadowColor);
		setRenderer(new TextRenderer(text, main, shadow));
	}
	
	@Override
	public void update(float delta) {
		bounceBehavior.update(this, delta);
		lifetime.update(delta);
	}
}
