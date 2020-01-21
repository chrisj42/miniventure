package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	// TODO add EntityItem, an item that becomes an entity when placed (like TileItems become tiles when placed). This will be used for small placeable items that can be stored in an inventory.
	
	// Tools are used to interact with the world, usually through destruction.
	// Tools have durability, and multiple levels of quality.
	Tool(data -> new ToolItem(ToolItem.ToolType.valueOf(data[0]), MaterialQuality.valueOf(data[1]), Integer.parseInt(data[2]))),
	
	Hammer(data -> HammerType.valueOf(data[0]).get()),
	
	Food(data -> FoodType.valueOf(data[0]).get()),
	
	Placeable(data -> PlaceableItemType.valueOf(data[0]).get()),
	
	Resource(data -> ResourceType.valueOf(data[0]).get()),
	
	Ephemeral(data -> {throw new UnsupportedOperationException("Ephemeral items cannot be loaded.");});
	
	
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
	
	public static abstract class EphemeralItem extends ServerItem {
		
		protected EphemeralItem(@NotNull String name) {
			super(Ephemeral, name);
		}
		protected EphemeralItem(@NotNull String name, @NotNull TextureHolder texture) {
			super(Ephemeral, name, texture);
		}
		
		@Override
		public final String[] save() {
			throw new UnsupportedOperationException("Ephemeral items cannot be saved.");
		}
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
			super(type, enumValue.name(), texture);
			// this.enumItemType = type;
			// this.enumValue = enumValue;
			saveData = new String[] {type.name(), enumValue.name()};
		}
		
		@Override
		public String[] save() {
			return saveData;
		}
		
	}
}
