package miniventure.game.api;

import java.util.EnumMap;

public class ObjectType<T extends Enum<T> & APIObject<T, P>, P extends Property<P>> {
	
	private final Class<T> typeClass;
	
	// has a map of instances of the class to property lists
	private final EnumMap<T, TypeInstance<T, P>> instances;
	
	ObjectType(Class<T> typeClass) {
		this.typeClass = typeClass;
		this.instances = new EnumMap<>(typeClass);
	}
	
	void initialize(PropertyFetcher<P> defaultProperties) {
		PropertyInstanceMap<P> defaultProps = new PropertyInstanceMap<>(defaultProperties);
		
		for(T enumVal: typeClass.getEnumConstants())
			instances.put(enumVal, new TypeInstance<>(this, enumVal, defaultProps));
	}
	
	public TypeInstance<T, P> getTypeInstance(T instance) {
		return instances.get(instance);
	}
	
	public <Q extends P> Q getProp(T instance, Class<Q> property) { return getTypeInstance(instance).getProp(property); }
}
