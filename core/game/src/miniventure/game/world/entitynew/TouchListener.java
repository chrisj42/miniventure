package miniventure.game.world.entitynew;

@FunctionalInterface
public interface TouchListener extends EntityProperty {
	
	boolean touchedBy(Entity other, boolean initial, Entity e);
	
}
