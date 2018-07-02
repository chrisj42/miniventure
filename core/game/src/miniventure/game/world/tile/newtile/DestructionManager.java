package miniventure.game.world.tile.newtile;

import miniventure.game.item.Item;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.newtile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructionManager {
	
	static final DestructionManager INDESTRUCTIBLE(@NotNull TileTypeEnum tileType) {
		return new DestructibleBuilder(tileType, -1).require(item -> false).make();
	}
	
	static final int HEALTH_IDX = 0;
	
	final int totalHealth;
	
	@NotNull private final PreferredTool[] preferredTools;
	@NotNull private final DamageConditionCheck[] damageConditions;
	@NotNull final ItemDrop[] drops;
	
	// main constructor
	DestructionManager(@NotNull TileTypeEnum tileType, int totalHealth, @NotNull PreferredTool[] preferredTools, @NotNull DamageConditionCheck[] damageConditions, @NotNull ItemDrop[] drops) {
		this.totalHealth = totalHealth;
		this.preferredTools = preferredTools;
		this.damageConditions = damageConditions;
		this.drops = drops;
	}
	
	// for those with health, preferred tools, and drops a tile item.
	DestructionManager(@NotNull TileTypeEnum tileType, int totalHealth, PreferredTool preferredTool) {
		this(tileType, totalHealth, preferredTool, new ItemDrop(TileItem.get(tileType)));
	}
	// above, but custom drop
	DestructionManager(@NotNull TileTypeEnum tileType, int totalHealth, PreferredTool preferredTool, ItemDrop... drops) {
		this(tileType, totalHealth, new PreferredTool[] {preferredTool}, new DamageConditionCheck[0], drops);
	}
	
	// for those that are one-shot, and have certain damage conditions, and drop a tile item
	DestructionManager(@NotNull TileTypeEnum tileType, DamageConditionCheck... damageConditions) {
		this(tileType, new ItemDrop(TileItem.get(tileType)), damageConditions);
	}
	// above, but custom single item drop
	DestructionManager(@NotNull TileTypeEnum tileType, @NotNull ItemDrop drop, DamageConditionCheck... damageConditions) {
		this(tileType, 1, new PreferredTool[0], damageConditions, new ItemDrop[] {drop});
	}
	
	DestructionManager(DestructionManager model) {
		totalHealth = model.totalHealth;
		drops = model.drops;
		preferredTools = model.preferredTools;
		damageConditions = model.damageConditions;
	}
	
	public static class DestructibleBuilder {
		@NotNull
		private TileTypeEnum type;
		private int health;
		private PreferredTool[] preferredTools;
		private DamageConditionCheck[] damageConditions;
		private ItemDrop[] drops;
		
		public DestructibleBuilder(@NotNull TileTypeEnum type) { this(type, 1); }
		public DestructibleBuilder(@NotNull TileTypeEnum type, int health) { this(type, health, health == 1); }
		public DestructibleBuilder(@NotNull TileTypeEnum type, boolean dropTileItem) { this(type, 1, dropTileItem); }
		public DestructibleBuilder(@NotNull TileTypeEnum type, int health, boolean dropTileItem) {
			this.type = type;
			this.health = health;
			preferredTools = new PreferredTool[0];
			damageConditions = new DamageConditionCheck[0];
			if(dropTileItem)
				drops = new ItemDrop[] { new ItemDrop(TileItem.get(type)) };
			else
				drops = new ItemDrop[0];
		}
		
		public DestructibleBuilder(@NotNull TileTypeEnum type, DestructionManager model) {
			this.type = type;
			this.health = model.totalHealth;
			this.drops = model.drops;
			this.preferredTools = model.preferredTools;
			this.damageConditions = model.damageConditions;
		}
		
		public DestructibleBuilder tileType(@NotNull TileTypeEnum type) { this.type = type; return this; }
		
		public DestructibleBuilder health(int health) { this.health = health; return this; }
		
		public DestructibleBuilder prefer(PreferredTool... preferredTools) { return prefer(true, preferredTools); }
		public DestructibleBuilder prefer(boolean replace, PreferredTool... preferredTools) {
			if(replace || this.preferredTools.length == 0)
				this.preferredTools = preferredTools;
			else if(preferredTools.length > 0)
				this.preferredTools = MyUtils.arrayJoin(PreferredTool.class, this.preferredTools, preferredTools);
			return this;
		}
		
		public DestructibleBuilder require(DamageConditionCheck... damageConditions) { return require(true, damageConditions); }
		public DestructibleBuilder require(boolean replace, DamageConditionCheck... damageConditions) {
			if(replace || this.damageConditions.length == 0)
				this.damageConditions = damageConditions;
			else if(damageConditions.length > 0)
				this.damageConditions = MyUtils.arrayJoin(DamageConditionCheck.class, this.damageConditions, damageConditions);
			return this;
		}
		
		public DestructibleBuilder drops(boolean dropTileItem) { return drops(true, dropTileItem); }
		public DestructibleBuilder drops(boolean replace, boolean dropTileItem) {
			if(dropTileItem)
				return drops(replace, new ItemDrop(TileItem.get(type)));
			else
				return drops(replace, new ItemDrop[0]);
		}
		public DestructibleBuilder drops(@NotNull ItemDrop... drops) { return drops(true, drops); }
		public DestructibleBuilder drops(boolean replace, @NotNull ItemDrop... drops) {
			if(replace || this.drops.length == 0)
				this.drops = drops;
			else if(drops.length > 0)
				this.drops = MyUtils.arrayJoin(ItemDrop.class, this.drops, drops);
			return this;
		}
		
		public DestructionManager make() {
			return new DestructionManager(type, health, preferredTools, damageConditions, drops);
		}
	}
	
	boolean tileAttacked(@NotNull Tile tile, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		return false;
	}
	
	int getDamage(@Nullable Item attackItem, int damage) {
		if(damageConditions.length > 0) {
			// must satisfy at least one condition
			boolean doDamage = true;
			for(DamageConditionCheck condition: damageConditions)
				doDamage = doDamage && condition.isDamagedBy(attackItem);
			
			if(!doDamage) return 0;
			// otherwise, continue.
		}
		
		if(preferredTools.length > 0 && attackItem instanceof ToolItem) {
			ToolType type = ((ToolItem)attackItem).getToolType();
			for(PreferredTool preference: preferredTools)
				if(type == preference.toolType)
					damage = (int) Math.ceil(damage * preference.damageMultiplier);
		}
		
		return damage;
	}
	
	@Override
	public String[] getInitialData() {
		if(totalHealth > 1) return new String[] {totalHealth+""};
		return new String[0]; // for a health of one or below, the tile will always be at max health, or destroyed.
	}
	
	
	
	static class PreferredTool {
		
		private final ToolType toolType;
		private final float damageMultiplier;
		
		public PreferredTool(@NotNull ToolType toolType, float damageMultiplier) {
			this.toolType = toolType;
			this.damageMultiplier = damageMultiplier;
		}
		
		public ToolType getToolType() { return toolType; }
		public float getDamageMultiplier() { return damageMultiplier; }
		
	}
	
	@FunctionalInterface
	interface DamageConditionCheck {
		boolean isDamagedBy(@Nullable Item attackItem);
	}
	
	static class RequiredTool implements DamageConditionCheck {
		
		@Nullable private final ToolType toolType;
		@Nullable private final Material material;
		
		public RequiredTool(@Nullable ToolType toolType) {
			this(toolType, null);
		}
		public RequiredTool(@Nullable ToolType toolType, @Nullable Material material) {
			this.toolType = toolType;
			this.material = material;
		}
		
		@Override
		public boolean isDamagedBy(@Nullable Item attackItem) {
			if(attackItem == null || !(attackItem instanceof ToolItem))
				return false;
			
			ToolItem tool = (ToolItem) attackItem;
			return (toolType == null || tool.getToolType() == toolType) && (material == null || tool.getMaterial() == material);
		}
	}
	
	@Override
	public String toString() { return "DestructibleProperty[conditions="+damageConditions.length+"]"; }
}
