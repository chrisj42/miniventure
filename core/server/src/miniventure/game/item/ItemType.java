package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.Version;
import miniventure.game.world.entity.mob.player.HandItem;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	// TODO add EntityItem, an item that becomes an entity when placed (like TileItems become tiles when placed). This will mainly be used for various types of furniture.
	
	// tools could be under the Enum ItemType, it would just handle the data a little differently.
	Tool(data -> new ToolItem(ToolItem.ToolType.valueOf(data[0]), Material.valueOf(data[1]), Integer.parseInt(data[2]))),
	
	// Enum(data -> EnumItemType.valueOf(data[0]).getItem(data[1])),
	
	Food(data -> FoodType.valueOf(data[0]).get()),
	
	Tile(data -> TileItemType.valueOf(data[0]).get()),
	
	Resource(data -> ResourceType.valueOf(data[0]).get()),
	
	Hand(data -> HandItem.hand);
	
	
	private final ItemFetcher fetcher;
	ItemType(ItemFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	public ServerItem load(String[] data, @NotNull Version version) {
		return fetcher.load(data);
	}
	
	@FunctionalInterface
	interface ItemFetcher {
		ServerItem load(String[] data);
	}
	
	// utility class for Item types that are no more than an enum value.
	static abstract class EnumItem extends ServerItem {
		// private final EnumItemType enumItemType;
		// private final Enum<?> enumValue;
		
		private final String[] saveData;
		
		EnumItem(@NotNull ItemType type, @NotNull Enum<?> enumValue) {
			this(type, enumValue, GameCore.icons.get("items/"+type.name().toLowerCase()+'/'+enumValue.name().toLowerCase()));
		}
		EnumItem(@NotNull ItemType type, @NotNull Enum<?> enumValue, @NotNull TextureHolder texture) {
			super(type, enumValue.name(), true, texture);
			// this.enumItemType = type;
			// this.enumValue = enumValue;
			saveData = new String[] {getType().name(), type.name(), enumValue.name()};
		}
		
		@Override
		public String[] save() {
			return saveData;
		}
		
	}
}
