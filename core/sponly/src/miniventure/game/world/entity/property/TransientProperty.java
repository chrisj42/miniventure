package miniventure.game.world.entity.property;

public interface TransientProperty extends EntityProperty {
	// entity properties which will be added to and removed from an entity throughout its life
	
	boolean isFinished();
	
}
