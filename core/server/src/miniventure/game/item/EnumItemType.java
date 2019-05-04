package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.TileItem.TileItemType;
import miniventure.game.util.function.MapFunction;

import org.jetbrains.annotations.NotNull;

public enum EnumItemType {
	
	Food(name -> FoodType.valueOf(name).get()),
	
	Resource(name -> ResourceType.valueOf(name).get()),
	
	Tile(name -> TileItemType.valueOf(name).item);
	
	
	final MapFunction<String, ServerItem> itemFetcher;
	
	EnumItemType(MapFunction<String, ServerItem> itemFetcher) {
		this.itemFetcher = itemFetcher;
	}
	
	static class EnumItem extends ServerItem {
		private final EnumItemType enumItemType;
		private final Enum<?> enumValue;
		
		EnumItem(@NotNull EnumItemType type, @NotNull Enum<?> enumValue) {
			super(ItemType.Enum, enumValue.name(), true, GameCore.icons.get("items/"+type.name().toLowerCase()+'/'+enumValue.name().toLowerCase()));
			this.enumItemType = type;
			this.enumValue = enumValue;
		}
		
		@Override
		public String[] save() {
			return new String[] {getType().name(), enumItemType.name(), enumValue.name()};
		}
		
	}
}
