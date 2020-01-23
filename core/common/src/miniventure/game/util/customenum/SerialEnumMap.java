package miniventure.game.util.customenum;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import miniventure.game.util.MyUtils;

@SuppressWarnings("unchecked")
public class SerialEnumMap<SET extends SerialEnum<?, ?>> extends GEnumMap<SET> {
	
	public SerialEnumMap() {}
	public SerialEnumMap(DataEntry<?, ? extends SET>... entries) { super(entries); }
	public SerialEnumMap(GEnumMap<SET> model) { super(model); }
	
	public String serialize(boolean save) {
		ArrayList<String> entries = new ArrayList<>(map.size());
		
		for(SerialEnum key: map.keySet())
			entries.add(key.name()+'='+key.serialize(map.get(key)));
		
		return MyUtils.encodeStringArray(entries);
	}
	
	/*public static <SET extends SerialEnum<?, SET>> SerialEnumMap<SET> deserialize(String alldata, Class<SET> tagClass) {
		String[] data = MyUtils.parseLayeredString(alldata);
		
		SerialEntry<?>[] entries = new SerialEntry[data.length];
		for(int i = 0; i < entries.length; i++)
			entries[i] = SerialEntry.deserialize(data[i], tagClass);
		
		return new SerialEnumMap(entries);
	}*/
}
