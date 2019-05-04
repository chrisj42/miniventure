package miniventure.game.item;

import miniventure.game.item.EnumItemType.EnumItem;

import org.jetbrains.annotations.NotNull;

public enum ResourceType {
	
	Log, Flint, Coal, Cotton, Fabric, Stone, Iron, Tungsten, Ruby;
	
	@NotNull
	public ServerItem get() {
		return new EnumItem(EnumItemType.Resource, this);
	}
	
}
