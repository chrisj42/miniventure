package miniventure.game.world.property;

// property of a world object
public interface WorldObjectProperty {
	
	/*
		- world object property objects are created per object type
		- each property object defines data that is specific to each object instance
		- like tile types, some data is type-local, and those are fields in property classes
		- but some data is instance-local
			- this data is shared between all properties(?)
			- it's like the entity has property types, and then instance data types
		- properties must declare which data types they will be actively managing
		
		
		- each property declares the type of each bit of data they will use
			- easy way to simplify this is to create an enum in each data class, and each constant holds a type. instead of giving types directly, properties are allowed to give a list of enum constants that implement a PropertyDataType interface, which provides a way to fetch the data type.
	 */
	
	
}
