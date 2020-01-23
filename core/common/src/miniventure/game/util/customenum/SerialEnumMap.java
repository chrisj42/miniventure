package miniventure.game.util.customenum;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import miniventure.game.util.MyUtils;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public class SerialEnumMap<SET extends SerialEnum> extends GEnumMap<SET> {
	
	public SerialEnumMap() {}
	public SerialEnumMap(DataEntry<?, ? extends SET>... entries) { super(entries); }
	public SerialEnumMap(SerialEnumMap<SET> model) { super(model); }
	
	public <CT extends SET> SerialEnumMap(String alldata, Class<SET> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		for(String item: data)
			SerialEnum.deserializeEntry(item, tagClass, this);
	}
	
	public String serialize(boolean save) {
		ArrayList<String> entries = new ArrayList<>(map.size());
		
		for(SET key: map.keySet()) {
			if(key.save && save || key.send && !save)
				entries.add(key.serializeEntry(this));
		}
		
		return MyUtils.encodeStringArray(entries);
	}
}
