package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.particle.ParticleData.ActionParticleData;

import org.jetbrains.annotations.NotNull;

public class ActionParticle extends ClientParticle {
	
	// may be animated; lasts as long as the animation, or lasts a given time, with a single frame.
	
	@NotNull private final LifetimeTracker lifetime;
	
	ActionParticle(ActionParticleData data) {
		super();
		
		float animationTime = data.action.animationTime;
		lifetime = new LifetimeTracker(this, animationTime, false);
		
		String spriteName = data.action.getSpriteName(data.dir);
		setRenderer(new AnimationRenderer(spriteName, animationTime, false, false));
	}
	
	@Override
	public void update(float delta) {
		lifetime.update(delta);
	}
}
