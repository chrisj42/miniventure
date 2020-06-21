package miniventure.game.item;

import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.world.entity.mob.player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public enum ResourceType implements ItemEnum {
	
	Log, Flint, Coal, Cotton, Fabric, Stone, Iron, Tungsten, Ruby;
	
	@NotNull
	private final Item item;
	
	ResourceType() {
		item = new EnumItem(ItemType.Resource, this) {
			@Override @NotNull
			public CursorHighlight getHighlightMode() {
				return CursorHighlight.INVISIBLE;
			}
		};
	}
	
	@Override @NotNull
	public Item get() {
		return item;
	}
	
}
