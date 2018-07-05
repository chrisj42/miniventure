package miniventure.game.world.tile.data;

import java.lang.reflect.InvocationTargetException;

class DataEntry<T, D extends DataTag<T>> {
	
	final D key;
	final T value;
	
	DataEntry(D dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
	
	String serialize() {
		return key.name()+','+key.serialize(value);
	}
	
	@SuppressWarnings("unchecked")
	static <D extends DataTag<?>> DataEntry<?, D> deserialize(String data, Class<D> tagClass) {
		String key = data.substring(0, data.indexOf(','));
		String value = data.substring(data.indexOf(',')+1);
		
		D tag;
		try {
			//noinspection JavaReflectionMemberAccess
			tag = (D) tagClass.getMethod("valueOf", String.class).invoke(null, key);
		} catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("All classes extending DataTag must have a static valueOf(String) method that returns an instance of the class; "+tagClass+" does not.");
		}
		
		return (DataEntry<?, D>) tag.serialEntry(value);
	}
}
