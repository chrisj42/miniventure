package miniventure.game.item;

import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;

public class ToolItem extends Item {
	
	public enum Material {
		Wood(20, 1, 4),
		
		Stone(80, 2, 3),
		
		Iron(250, 4, 2),
		
		Gem(800, 8, 1);
		
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
		super(material.name() + " " + type.name(), type.texture);
		this.toolType = type;
		this.material = material;
		this.durability = durability;
	}
	
	@Override public int getMaxStackSize() { return 1; }
	
	public ToolType getType() { return toolType; }
	public Material getMaterial() { return material; }
	
	@Override
	public ToolItem use() {
		if(durability > 1)
			return new ToolItem(toolType, material, durability-1);
		
		return null;
	}
	
	@Override
	public int getStaminaUsage() { return material.staminaUsage; }
	
	// note, the tool will not specify how much damage it does to a tile, generally. 
	@Override
	public int getDamage(WorldObject target) {
		int damage = 1;
		if(target instanceof Mob) {
			if(toolType == ToolType.Sword)
				damage = 3;
			if(toolType == ToolType.Axe)
				damage = 2;
		}
		
		return damage * material.damageMultiplier;
	}
	
	// TODO when i have a fishing rod, I can make it use the interact method. Or, I can give the water tile an interactable property. I think that might be cleaner.
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && ((ToolItem)other).durability == durability;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + durability; }
	
	@Override
	public ToolItem copy() { return new ToolItem(toolType, material, durability); }
}
