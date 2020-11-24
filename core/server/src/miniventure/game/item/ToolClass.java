package miniventure.game.item;

import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public enum ToolClass {
	Pickaxe(CursorHighlight.TILE_ADJACENT, 1),
	Shovel(CursorHighlight.TILE_ADJACENT, 2), // one swing per tile instead of many, so weight it more
	Axe(CursorHighlight.TILE_ADJACENT, 1),
	Club(CursorHighlight.FRONT_AREA, 3),
	Sword(CursorHighlight.FRONT_AREA, 1);
	
	@NotNull
	final CursorHighlight cursorType;
	final int staminaUsage;
	
	ToolClass(@NotNull CursorHighlight cursorType, int staminaUsage) {
		this.cursorType = cursorType;
		this.staminaUsage = staminaUsage;
	}
}
