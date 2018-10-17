package miniventure.game.item;

import miniventure.game.item.ItemType.SimpleEnumItem;

import org.jetbrains.annotations.NotNull;

public enum ResourceType {
	
	Log, Flint, Coal, Cotton, Fabric, Stone, Iron, Tungsten, Ruby;
	
	@NotNull
	public Item get() {
		return new ResourceItem();
	}
	
	class ResourceItem extends SimpleEnumItem {
		ResourceItem() {
			super(ItemType.Resource, name());
		}
	}
}
