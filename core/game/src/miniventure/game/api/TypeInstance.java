package miniventure.game.api;

import com.badlogic.gdx.utils.Array;

public class TypeInstance<T extends Enum<T> & APIObject<T, P>, P extends Property<P>> {
	
	private final ObjectType<T, P> typeInstance;
	private final T instance;
	
	private final PropertyInstanceMap<P> propertyMap;
	private final PropertyMap<P, Integer> propertyDataIndexes = new PropertyMap<>();
	private final PropertyMap<P, Integer> propertyDataLengths = new PropertyMap<>();
	private String[] initialData;
	
	public TypeInstance(ObjectType<T, P> typeInstance, T instance, PropertyInstanceMap<P> defaultProperties) {
		this.instance = instance;
		this.typeInstance = typeInstance;
		
		propertyMap = new PropertyInstanceMap<>(defaultProperties);
		
		Array<String> initData = new Array<>();
		
		for(P prop: instance.getProperties()) {
			propertyDataIndexes.put(prop, initData.size);
			String[] propData = prop.getInitialData();
			propertyDataLengths.put(prop, propData.length);
			initData.addAll(propData);
		}
		
		initialData = new String[initData.size];
		for(int i = 0; i < initialData.length; i++)
			initialData[i] = initData.get(i);
	}
	
	public <Q extends P> Q getProp(Class<Q> clazz) { return propertyMap.get(clazz); }
	
	int getDataLength() { return initialData.length; }
	
	String[] getInitialData() {
		String[] data = new String[initialData.length];
		System.arraycopy(initialData, 0, data, 0, data.length);
		
		return data;
	}
	
	void checkDataAccess(Class<? extends P> property, int propDataIndex) {
		// technically, the below should never happen, unless it's passed the TileProperty class or a dynamically generated class, or something, because the propertyMap should have an instance of each implementer of the TileProperty interface.
		if(!propertyDataIndexes.containsKey(property))
			throw new IllegalArgumentException("The specified property class, " + property + ", is not part of the list of TileType property classes.");
		
		if(propDataIndex >= propertyDataLengths.get(property))
			throw new IllegalArgumentException("Tile property " + property + " tried to access index past stated length; length="+propertyDataLengths.get(property)+", index="+propDataIndex);
	}
	
	int getPropDataIndex(Class<? extends P> prop) { return propertyDataIndexes.get(prop); }
	int getPropDataLength(Class<? extends P> prop) { return propertyDataLengths.get(prop); }
	
}
