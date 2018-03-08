package miniventure.game.world.entitynew.mod;

import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.InstanceData;
import miniventure.game.world.entitynew.InstanceData.FloatValue;
import miniventure.game.world.entitynew.property.UpdateProperty;

public class LifetimeProperty implements UpdateProperty {
	
	
	
	public LifetimeProperty(float lifetime) {
		
	}
	
	@Override
	public void update(Entity e, float delta) {
		
	}
	
	@Override
	public InstanceData getInitialDataObject() {
		return new FloatValue();
	}
	
	@Override
	public String[] getInitialData() {
		return new String[] {"0"};
	}
}
