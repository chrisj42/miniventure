package miniventure.game.item;

import miniventure.game.item.ToolType.ToolItem;
import miniventure.game.texture.FetchableTextureHolder;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	// TODO add EntityItem, an item that becomes an entity when placed (like TileItems become tiles when placed). This will be used for small placeable items that can be stored in an inventory.
	
	// Tools are used to interact with the world, usually through destruction.
	// Tools have durability, and multiple levels of quality.
	Tool(ToolItem::load),
	
	Hammer(data -> new HammerItem(
		ObjectRecipeSet.valueOf(data[0]),
		data[1].equals("null") ? null : Integer.parseInt(data[1])
	)),
	
	Food(data -> FoodType.valueOf(data[0]).get()),
	
	Placeable(data -> PlaceableItemType.valueOf(data[0]).get()),
	
	Resource(data -> ResourceType.valueOf(data[0]).get()),
	
	Ephemeral(data -> {throw new UnsupportedOperationException("Ephemeral items cannot be loaded.");});
	
	
	// private final ItemSerialType serialType;
	private final ItemFetcher fetcher;
	
	// ItemDataType(ItemFetcher fetcher) { this(ItemSerialType.Normal, fetcher); }
	ItemType(/*ItemSerialType serialType, */ItemFetcher fetcher) {
		// this.serialType = serialType;
		this.fetcher = fetcher;
	}
	
	public ServerItem load(String[] data, @NotNull Version version) {
		return fetcher.load(data);
	}
	
	// public ItemSerialType getSerialType() { return serialType; }
	
	@FunctionalInterface
	interface ItemFetcher {
		ServerItem load(String[] data);
	}
	
	public static abstract class EphemeralItem extends ServerItem {
		
		/*protected EphemeralItem(@NotNull String name) {
			super(Ephemeral, name);
		}*/
		protected EphemeralItem(@NotNull String name, @NotNull FetchableTextureHolder texture) {
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
			super(type, enumValue.name(), type.name().toLowerCase());
			saveData = new String[] {type.name(), enumValue.name()};
		}
		EnumItem(@NotNull ItemType type, @NotNull Enum<?> enumValue, @NotNull FetchableTextureHolder texture) {
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
