package miniventure.game.world.entitynew;

public interface SolidProperty extends EntityProperty {
	
	boolean isPermeableBy(Entity other, Entity e);
	
}
