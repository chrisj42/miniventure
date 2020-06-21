package miniventure.game.item;

import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.mob.player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public class ToolItem extends Item implements BreakableItem {
	
	// todo determine if I want to have all tools follow the same material/upgrade path, or let them deviate
	// - if deviate, determine how to provide levels; perhaps just a fetcher that is provided the tier?
		// - perhaps I want to give each tool type an enum for the levels, where they all implement a "ToolLevel" interface or something
	
	/* Tool aspects:
		- base damage: ToolType; provide an ineffective and effective damage stat
		- 
		
	 */
	
	public enum ToolType {
		Pickaxe(CursorHighlight.TILE_ADJACENT, 1),
		Shovel(CursorHighlight.TILE_ADJACENT, 2), // one swing per tile instead of many, so weight it more
		Axe(CursorHighlight.TILE_ADJACENT, 1),
		Sword(CursorHighlight.FRONT_AREA, 1);
		
		@NotNull
		private final CursorHighlight cursorType;
		private final int staminaUsage;
		
		ToolType(@NotNull CursorHighlight cursorType, int staminaUsage) {
			this.cursorType = cursorType;
			this.staminaUsage = staminaUsage;
		}
	}
	
	private final ToolType toolType;
	private final MaterialQuality quality;
	private final int durability;
	
	public ToolItem(ToolType type, MaterialQuality quality) { this(type, quality, quality.maxDurability); }
	ToolItem(ToolType type, MaterialQuality quality, int durability) {
		super(ItemType.Tool, quality.spriteName + '_' + type.name(), "tools");
		this.toolType = type;
		this.quality = quality;
		this.durability = durability;
	}
	
	public ToolType getToolType() { return toolType; }
	public MaterialQuality getQuality() { return quality; }
	
	@Override
	public ToolItem getUsedItem() {
		if(durability > 1)
			return new ToolItem(toolType, quality, durability-1);
		
		return null;
	}
	
	@Override
	public int getStaminaUsage() { return toolType.staminaUsage; }
	
	@Override
	public float getDurability() { return durability / (float) quality.maxDurability; }
	
	@Override @NotNull
	public CursorHighlight getHighlightMode() {
		return toolType.cursorType;
	}
	
	@Override
	public String compileSaveData() {
		return MyUtils.encodeStringArray(toolType.name(), quality.name(), String.valueOf(durability));
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && ((ToolItem)other).durability == durability;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + durability; }
	
	@Override public String toString() { return "ToolItem("+ quality +' '+toolType+",dura="+durability+')'; }
}
