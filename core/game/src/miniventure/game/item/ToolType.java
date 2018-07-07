package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;

public enum ToolType {
	
	Pickaxe, Shovel, Axe, Sword;
	
	public final TextureHolder texture = GameCore.icons.get("gem-sword");
}
