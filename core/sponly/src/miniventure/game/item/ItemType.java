package miniventure.game.item;

import miniventure.game.item.recipe.ObjectRecipeSet;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.HammerItem;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	// TODO add EntityItem, an item that becomes an entity when placed (like TileItems become tiles when placed). This will be used for small placeable items that can be stored in an inventory.
	
	// Tools are used to interact with the world, usually through destruction.
	// Tools have durability, and multiple levels of quality.
	Tool(data -> {
		String[] dataAr = MyUtils.parseLayeredString(data);
		return new ToolItem(ToolItem.ToolType.valueOf(dataAr[0]), MaterialQuality.valueOf(dataAr[1]), Integer.parseInt(dataAr[2]));
	}),
	
	Hammer(data -> new HammerItem(ObjectRecipeSet.valueOf(data))),
	
	Food(FoodType.class),
	
	Placeable(PlaceableItemType.class),
	
	Resource(ResourceType.class),
	
	Ephemeral(data -> {throw new UnsupportedOperationException("Ephemeral items cannot be loaded.");});
	
	
	@FunctionalInterface
	interface ItemParser {
		Item load(String data, Version dataVersion);
	}
	
	public final ItemParser loader;
	
	ItemType(ItemParser parser) {
		this.loader = parser;
	}
	ItemType(MapFunction<String, Item> parser) {
		this.loader = (data, dataVersion) -> parser.get(data);
	}
	<T extends Enum<T> & ItemEnum> ItemType(Class<T> enumClass) {
		loader = (data, dataVersion) -> Enum.valueOf(enumClass, data).get();
	}
	
	public static abstract class EphemeralItem extends Item {
		
		/*protected EphemeralItem(@NotNull String name) {
			super(Ephemeral, name);
		}*/
		protected EphemeralItem(@NotNull String name, @NotNull TextureHolder texture) {
			super(Ephemeral, name, texture);
		}
		
		@Override
		public final String compileSaveData() {
			throw new UnsupportedOperationException("Ephemeral items cannot be saved.");
		}
	}
	
	// utility class for Item types that are no more than an enum value.
	public static abstract class EnumItem extends Item {
		
		private final Enum<?> enumValue;
		
		EnumItem(@NotNull ItemType type, @NotNull Enum<?> enumValue) {
			super(type, enumValue.name(), type.name().toLowerCase());
			this.enumValue = enumValue;
		}
		EnumItem(@NotNull ItemType type, @NotNull Enum<?> enumValue, @NotNull TextureHolder texture) {
			super(type, enumValue.name(), texture);
			this.enumValue = enumValue;
		}
		
		@Override
		public String compileSaveData() { return enumValue.name(); }
	}
}
