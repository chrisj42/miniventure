package miniventure.game.api;

import java.util.HashMap;

public class TypeLoader {
	
	private static final HashMap<Class<? extends APIObject>, ObjectType> registeredTypes = new HashMap<>();
	
	public static <T extends Enum<T> & APIObject<T, P>, P extends Property<P>> void loadType(Class<T> clazz, PropertyFetcher<P> defaultProperties) {
		ObjectType<T, P> type = new ObjectType<>(clazz);
		registeredTypes.put(clazz, type);
		type.initialize(defaultProperties);
	}
	
	static <T extends Enum<T> & APIObject<T, P>, P extends Property<P>> ObjectType<T, P> getType(Class<T> typeClass) {
		//noinspection unchecked
		return registeredTypes.get(typeClass);
	}
	
	// registered types is a (map of enum/APIObject subclasses to (enum maps of their values to type objects. ) ).
	
	/*
		Alright, so you call getProp, getDataLength, etc. on this class; it uses the enum type you pass in to determine what set to get it from.
	 */
	
	//interface ObjectType<T extends Enum<T>> extends Comparable<T> {
		
	//}
	
	// there will be a map of object type classes to ObjectType TreeMaps, which contain all the instances of that type. You call TypeLoader.getProp(objInstance, Prop.class).
	// to register the types, you should create a type builder, given the default types, and repeatedly call addType with an instance of the type class, and a list of properties.
	// after all have been given, you call register, which puts it here. or something.
	
	/*static <T, P extends Property> TypeBuilder<T, P> buildObjectType(Class<T> typeClass, PropertyAdder<P> defaultProperties) {
		return new TypeBuilder<>(typeClass, defaultProperties);
	}
	
	/// This is how other classes access the properties of the types.
	static <T, P extends Property, Q extends P> Q getProp(T typeInstance, Class<Q> propClass) {
		return getProp(getType(typeInstance), typeInstance, propClass);
	}
	
	/// Actually, it will probably be this one.
	public static <T, P extends Property, Q extends P> Q getProp(Class<T> typeClass, Object typeInstance, Class<Q> propClass) {
		return getProp(getType(typeInstance), typeInstance, propClass);
	}
	
	*//*public static <T> ObjectType<T, ? extends Property> getType(T typeInstance) {
		//noinspection unchecked
		return (ObjectType<T, ? extends Property>) registeredTypes.get(typeInstance.getClass());
	}*//*
	
	public static <T, P extends Property, Q extends P> Q getProp(ObjectType<T, P> type, T instance, Class<Q> propClass) {
		return type.getProp(instance, propClass);
	}
	
	// here, you register the types: EntityType, TileType, and ItemType.
	
	//private static HashMap<Class<?>, Class<? extends Property>> registeredTypePropertyClasses = new HashMap<>();
	
	// called by TypeBuilder as the last step to create the type.
	static <T, P extends Property> void addObjectType(ObjectType<T, P> type) {
		registeredTypes.put(type.typeClass, type);
		//registeredTypePropertyClasses.put(type.typeClass, type.propClass);
	}
	
	*//*
		Referencing the objects can be done any way, but their info is stored here..?
		
		TypeLoader.getProp(ItemType.Tool, Property property)
		
	 *//*
	
	// the class of the first argument should be in the map for type classes
		// the first argument itself should be in that type class's instance to property map
	
	private static class TypeMap<T extends Enum<T> & APIObject<T, ? extends Property>> extends HashMap<Class<T>, ObjectType<T, >> {
		public TypeMap() {
			super();
		}
	}*/
	
}
