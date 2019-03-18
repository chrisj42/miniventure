package miniventure.game.world.entity.particle;

import miniventure.game.GameProtocol.ParticleAddition;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.world.entity.ClientEntity;

import com.badlogic.gdx.math.Vector2;

import static miniventure.game.world.entity.particle.ParticleData.ActionParticleData;
import static miniventure.game.world.entity.particle.ParticleData.TextParticleData;

public abstract class ClientParticle extends ClientEntity {
	
	ClientParticle() {}
	
	@Override
	public abstract void update(float delta);
	
	public static ClientParticle get(ParticleAddition addition) {
		final ParticleData data = addition.particleData;
		ClientParticle particle = null;
		
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
	}
}
