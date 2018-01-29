package miniventure.game.item;

import miniventure.game.GameCore;

import org.jetbrains.annotations.NotNull;

public enum ResourceItem {
	
	Log;
	
	@NotNull
	public Item get() {
		return new Item(name(), GameCore.icons.get(name().toLowerCase()));
	}
}
