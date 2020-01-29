package miniventure.game.item;

import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.texture.ItemTextureSource;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;

public class ItemDataTag<T> extends SerialEnum<T, ItemDataTag<T>> {
	
	public static final ItemDataTag<Float> Usability = new ItemDataTag<>(Float.class);
	
	public static final ItemDataTag<EquipmentType> EquipmentType = new ItemDataTag<>(EquipmentType.class);
	
	public static final ItemDataTag<FetchableTextureHolder> CursorSprite = new ItemDataTag<FetchableTextureHolder>(tex -> MyUtils.encodeStringArray(
		String.valueOf(tex.source.ordinal()),
		tex.tex.name
	), data -> {
		String[] parts = MyUtils.parseLayeredString(data);
		ItemTextureSource source = ItemTextureSource.values[Integer.parseInt(parts[0])];
		return source.get(parts[1]);
	});
	
	private ItemDataTag(Class<T> clazz) {
		super(false, true, clazz);
	}
	
	private ItemDataTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(false, true, valueWriter, valueParser);
	}
	
}