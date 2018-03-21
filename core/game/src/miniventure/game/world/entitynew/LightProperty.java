package miniventure.game.world.entitynew;

@FunctionalInterface
public interface LightProperty extends EntityProperty {
	
	float getLightRadius(Entity e);
	
}
