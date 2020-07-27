package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.ClientEntityRenderer.TextRenderer;
import miniventure.game.world.entity.particle.ParticleData.TextParticleData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class TextParticle extends ClientParticle {
	
	@NotNull private final BounceBehavior bounceBehavior;
	@NotNull private final LifetimeTracker lifetime;
	
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
	
	@Override
	public void remove() {
		super.remove();
		bounceBehavior.free();
	}
}
