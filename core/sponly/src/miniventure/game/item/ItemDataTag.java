package miniventure.game.item;

import miniventure.game.item.recipe.ObjectRecipe;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;

public class ItemDataTag<T> extends SerialEnum<T, ItemDataTag<T>> {
	
	public static final ItemDataTag<ObjectRecipe> OBJECT_RECIPE = new ItemDataTag<>();
	
	private ItemDataTag() {
		super();
	}
	
	private ItemDataTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(valueWriter, valueParser);
	}
	
	private ItemDataTag(Class<T> valueClass) {
		super(valueClass);
	}
}
