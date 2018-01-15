package miniventure.game.item;

import miniventure.game.screen.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum ToolType {
	
	PICKAXE, SHOVEL, AXE, SWORD;
	
	public final TextureRegion texture = GameCore.icons.get("gem-sword");
}
