package miniventure.game.util.command;

import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldManager;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

public final class ArgumentSet<T> {
	
	// how about we start with server commands only...
	
	// types for individual arguments.
	@FunctionalInterface
	interface ArgumentType {
		
		ArgumentType INTEGER = (world, arg) -> MyUtils.noException(() -> Integer.parseInt(arg));
		ArgumentType DECIMAL = (world, arg) -> MyUtils.noException(() -> Double.parseDouble(arg));
		//ArgumentType PLAYER = (world, arg) -> 
		
		boolean valid(WorldManager world, String arg);
	}
	
	/*
		Alright, so command arguments can have a couple different properties.
			- they can be optional.
			- they can be of the form:
				- "--option" (not going to worry about)
				- @keyword (argument type)
				- reference to a player (by username)
				- reference to an entity, or a collection of entities (with @keyword)
				- reference to a position (poss. including level)
				
	 */
	
	public static final ArgumentSet<Player> PLAYER = new ArgumentSet<>();
	
	public static final ArgumentSet<Entity> ENTITY = new ArgumentSet<>();
	
	/*public static final ArgumentSet<ENTITY_POSITION,
		
		TILE_POSITION
	*/
}
