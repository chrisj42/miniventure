package miniventure.game.item;

import miniventure.game.GameCore;

public class ToolItem extends ServerItem {
	
	public enum ToolType {
		Pickaxe, Shovel, Axe, Sword;
	}
	
	public enum Material {
		Flint(50, 1, 1),
		
		Stone(120, 2, 1),
		
		Iron(250, 4, 1),
		
		Tungsten(600, 6, 1),
		
		Ruby(1500, 8, 1);
		
		public final int maxDurability; // the number of uses this level of tool gets.
		public final int damageMultiplier; // damage done by this tool is multiplied by this number.
		public final int staminaUsage; // the stamina points that are used up each use.
		
		Material(int maxDurability, int damageMultiplier, int staminaUsage) {
			this.maxDurability = maxDurability;
			this.damageMultiplier = damageMultiplier;
			this.staminaUsage = staminaUsage;
		}
	}
	
	private final ToolType toolType;
	private final Material material;
	private final int durability;
	
	public ToolItem(ToolType type, Material material) { this(type, material, material.maxDurability); }
	public ToolItem(ToolType type, Material material, int durability) {
		super(ItemType.Tool, material.name() + ' ' + type.name(), GameCore.icons.get("items/tools/"+material.name().toLowerCase()+'_'+type.name().toLowerCase()));
		this.toolType = type;
		this.material = material;
		this.durability = durability;
	}
	
	public ToolType getToolType() { return toolType; }
	public Material getMaterial() { return material; }
	
	@Override
	public ToolItem getUsedItem() {
		if(durability > 1)
			return new ToolItem(toolType, material, durability-1);
		
		return null;
	}
	
	@Override
	public int getStaminaUsage() { return material.staminaUsage; }
	
	// the the Item class will have the wrong value, but it only references it through this method so this override will negate any effects.
	@Override
	public float getUsabilityStatus() { return material == null ? 0 : durability / (float) material.maxDurability; }
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && ((ToolItem)other).durability == durability;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + durability; }
	
	@Override
	public String[] save() {
		return new String[] {getType().name(), toolType.name(), material.name(), String.valueOf(durability)};
	}
	
	@Override public String toString() { return "ToolItem("+material+' '+toolType+",dura="+durability+')'; }
}
