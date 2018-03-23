package miniventure.game.world.entity.mob;

import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;

public interface Mob extends WorldObject {
	
	Direction getDirection();
	
}
