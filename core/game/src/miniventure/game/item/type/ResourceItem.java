package miniventure.game.item.type;

import miniventure.game.GameCore;
import miniventure.game.item.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public enum ResourceItem {
	
	Log;
	
	@NotNull
	public Item get() {
		return new Item(name(), GameCore.icons.size() > 0 ? GameCore.icons.get(name().toLowerCase()) : new TextureRegion()) {
			@Override public Item copy() { return this; }
		};
	}
}
