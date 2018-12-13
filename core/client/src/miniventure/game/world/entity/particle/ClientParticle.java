package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.ClientEntity;

public abstract class ClientParticle extends ClientEntity implements ParticleData {
	
	protected ClientParticle() {
		
	}
	
	@Override
	public abstract void update(float delta);
	
	public static ClientParticle get(ParticleData data) {
		if(data instanceof ClientParticle) {
			System.err.println("WARNING: ClientParticle passed to own get instead of a data class: "+data);
			return (ClientParticle) data; // you shouldn't be doing this.
		}
		
		if(data instanceof ActionParticleData)
			return new ActionParticle((ActionParticleData) data);
		
		if(data instanceof TextParticleData)
			return new TextParticle((TextParticleData) data);
		
		throw new IllegalArgumentException("Unknown ClientParticles subclass: "+data.getClass()+" - object: "+data);
	}
}
