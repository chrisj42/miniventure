package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntitySpawn;

import org.jetbrains.annotations.NotNull;

public abstract class Particle extends Entity {
	
	private final LifetimeTracker lifetime;
	
	Particle(@NotNull EntitySpawn info, float lifetime, boolean blinkNearEnd) {
		super(info);
		this.lifetime = new LifetimeTracker(this, lifetime, blinkNearEnd);
	}
	
	@Override
	public void update(float delta) {
		lifetime.update(delta);
	}
	
	float getAge() { return lifetime.getTime(); }
	
	/*public static Particle get(ParticleAddition addition) {
		final ParticleData data = addition.particleData;
		Particle particle = null;
		
		if(data instanceof ActionParticleData)
			particle = new ActionParticle((ActionParticleData) data);
		
		if(data instanceof TextParticleData)
			particle = new TextParticle((TextParticleData) data);
		
		if(particle == null)
			throw new IllegalArgumentException("Unknown ClientParticle subclass: "+data.getClass()+" - object: "+data);
		
		PositionUpdate newPos = addition.positionUpdate;
		Vector2 size = particle.getSize();
		particle.moveTo(newPos.x - size.x/2, newPos.y - size.y/2, newPos.z); // center entity
		
		return particle;
	}*/
}
