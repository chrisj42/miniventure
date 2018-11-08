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
	
	@SuppressWarnings("unchecked")
	static <D extends SerialEnum<?>> SerialEntry<?> deserialize(String data, Class<D> tagClass) {
		String key = data.substring(0, data.indexOf(','));
		String value = data.substring(data.indexOf(',')+1);
		
		return GenericEnum.valueOf(tagClass, key).serialEntry(value);
	}
}
