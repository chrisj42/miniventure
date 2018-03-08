package miniventure.game.world.entitynew.mod;

import miniventure.game.item.Item;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.InstanceData;
import miniventure.game.world.entitynew.property.AttackListener;
import miniventure.game.world.entitynew.property.EntityProperty;
import miniventure.game.world.entitynew.property.UpdateProperty;

public class KnockbackProperty implements AttackListener, UpdateProperty {
	
	@Override
	public boolean attackedBy(WorldObject obj, Item heldItem, int damage, Entity e) {
		return false; // start knockback sequence
	}
	
	@Override
	public void update(Entity e, float delta) {
		// if knocked back, then move entity
	}
	
	// create specific data object
	
	@Override
	public InstanceData getInitialDataObject() {
		return null;
	}
	
	@Override
	public String[] getInitialData() {
		return new String[0];
	}
	
	@Override
	public Class<? extends EntityProperty> getUniquePropertyClass() {
		return KnockbackProperty.class;
	}
}
