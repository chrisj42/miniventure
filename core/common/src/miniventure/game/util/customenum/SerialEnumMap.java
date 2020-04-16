package miniventure.game.util.customenum;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import miniventure.game.util.MyUtils;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public class SerialEnumMap<SET extends SerialEnum> extends GEnumMap<SET> {
	
	public SerialEnumMap(Class<SET> enumClass) { super(enumClass); }
	public SerialEnumMap(Class<SET> enumClass, DataEntry<?, ? extends SET>... entries) { super(enumClass, entries); }
	public SerialEnumMap(DataEntry<?, ? extends SET> firstEntry, DataEntry<?, ? extends SET>... entries) { super(firstEntry, entries); }
	// public SerialEnumMap(SerialEnumMap<SET> model) { super(model); }
	
	public SerialEnumMap(String alldata, Class<SET> tagClass) {
		super(tagClass);
		String[] data = MyUtils.parseLayeredString(alldata);
		
		for(String item: data)
			SerialEnum.deserializeEntry(item, tagClass, this);
	}
	
	public String serialize(boolean save) {
		ArrayList<String> entries = new ArrayList<>(data.length);
		
		for(SET key: GenericEnum.values(dataClass)) {
			if(get(key) == null) continue;
			if(key.save && save || key.send && !save)
				entries.add(key.serializeEntry(this));
		}
		
		return MyUtils.encodeStringArray(entries);
	}
}
