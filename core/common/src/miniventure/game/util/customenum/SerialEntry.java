package miniventure.game.util.customenum;

class SerialEntry<T> {
	
	final SerialEnum<T> key;
	final T value;
	
	SerialEntry(SerialEnum<T> dataTag, T value) {
		this.key = dataTag;
		this.value = value;
	}
	
	String serialize() {
		return key.name()+','+ key.serialize(value);
	}
	
	// the generics here actually make a lot of sense; you're given a tag class (i.e. SerialEnum subclass) along with data about the instance, and you have to get an instance. But since instances have different generic types, you can't know what that type will be, so it's a question mark.
	static <ET extends SerialEnum<?>> SerialEntry<?> deserialize(String data, Class<ET> tagClass) {
		String key = data.substring(0, data.indexOf(','));
		String value = data.substring(data.indexOf(',')+1);
		
		return GenericEnum.valueOf(tagClass, key).serialEntry(value);
	}
}
