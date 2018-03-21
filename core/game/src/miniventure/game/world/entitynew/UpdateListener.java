package miniventure.game.world.entitynew;

@FunctionalInterface
public interface UpdateListener extends EntityProperty {
	
	void update(float delta, Entity e);
	
}
