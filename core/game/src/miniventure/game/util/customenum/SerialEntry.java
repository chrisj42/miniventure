package miniventure.game.util.customenum;

import java.lang.reflect.InvocationTargetException;

class SerialEntry<T, D extends SerialEnum<T>> {
	
	final D key;
	final T value;
	
	SerialEntry(D dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
	
	String serialize() {
		return key.name()+','+key.serialize(value);
	}
	
	@SuppressWarnings("unchecked")
	static <D extends SerialEnum<?>> SerialEntry<?, D> deserialize(String data, Class<D> tagClass) {
		String key = data.substring(0, data.indexOf(','));
		String value = data.substring(data.indexOf(',')+1);
		
		D tag;
		try {
			//noinspection JavaReflectionMemberAccess
			tag = (D) tagClass.getMethod("valueOf", String.class).invoke(null, key);
		} catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("All classes extending DataTag must have a static valueOf(String) method that returns an instance of the class; "+tagClass+" does not.");
		}
		
		return (SerialEntry<?, D>) tag.serialEntry(value);
	}
}
