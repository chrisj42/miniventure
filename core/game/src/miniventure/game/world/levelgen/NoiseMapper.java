package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType.TileTypeEnum;

public class NoiseMapper implements NoiseTileFetcher {
	
	// manages noise somehow; holds a list of a certain type of value that can return a tiletype, and takes in a certain number of values.
	// returns a tiletype from the input values, through one of the list of values. So, technically, it itself could be part of a list. But not the same list, that would cause infinite recursion.
	
	/*
list.
orders a list of components.
add add/remove elements to/from list
can add listeners for order change.
	- can add buttons above list
	- can add buttons for each element in the list, that will be passed the element they are for in their action listener method.
can specify a way to convert the element into a string, so that it may be dragged.
	- if this is not specified, then it will display an icon with up/down arrows, signifying that it should be clicked/dragged to reorder the element.

elements have no requirements in and of themselves, other than being Components.

	 */
	
	/*
		all values are parameters.
		parameters can have a set value (meaning it is specified in the manager) and a use value (which is a variable value used per call).
		
		scratch that, only a set value. parameters without a set value are made to be part of the list of expected parameters on call.
		
		this class has a method to fetch the tiletype from an x,y and list of parameters.
		there may be a little bit of common functionality that I can use...
		but the fact is that the parameter values may be for anything, including things that aren't accessible from this scope. So I can't really do that... oh wait, I know what I can do!
			When getTileType is called, it goes through the list of parameters and sets the "use" value to those in the object array. Then it calls the normal getTileType method with just the x,y.
			this class will have a getParameter(String name, Class type) method used to get the value of a parameter. The use value will be returned if present, otherwise the set value. If there is no set value, an IllegalStateException will be thrown.
		
		To specify parameters:
			this will be the same JList structure as all the others:
				- add param button at top
				- delete button for each parameter
				- edit button for list (this may either be per-item or the whole list at once; if editing one item causes another to need an update, then per-item buttons should be used.)
				- parameters have a name... and type. though the type will be hard to enforce. maybe parameter is abstract and each parameter type needs to provide an editor?
					- i do like that... it follows the pattern of list items providing editors.
	 */
	
	private Parameter[] params;
	
	public static class Parameter<T> {
		
		private T value;
		
		public Parameter() {
			recieve = true;
			value = null;
		}
		
		public Parameter(T value) {
			recieve = false;
			this.value = value;
		}
		
		public T getValue(Object param) {
			
		}
	}
	
	
	public NoiseMapper() {
		
	}
	
	@Override
	public TileTypeEnum getTileType(int x, int y, Object[] parameters) {
		return null;
	}
}
