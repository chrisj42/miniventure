package miniventure.game.item.typeold;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum ToolType {
	
	Pickaxe, Shovel, Axe, Sword;
	
	public final TextureRegion texture = GameCore.icons.get("gem-sword");
}
