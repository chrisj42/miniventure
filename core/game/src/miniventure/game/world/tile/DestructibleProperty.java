package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionParticle;
import miniventure.game.world.entity.particle.TextParticle;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructibleProperty implements TilePropertyInstance {
	
	static final DestructibleProperty INDESTRUCTIBLE(@NotNull TileType tileType) {
		return new DestructibleProperty(tileType, -1, (PreferredTool)null, item -> false);
	}
	
	private static final int HEALTH_IDX = 0;
	
	private final int totalHealth;
	
	@NotNull private final PreferredTool[] preferredTools;
	
	@NotNull private final DamageConditionCheck[] damageConditions;
	@NotNull private final ItemDrop[] drops;
	
	@NotNull private final TileType tileType;
	
	// main constructor
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable PreferredTool[] preferredTools, @Nullable DamageConditionCheck[] damageConditions, @Nullable ItemDrop... drops) {
		this.tileType = tileType;
		this.totalHealth = totalHealth;
		this.preferredTools = preferredTools == null ? new PreferredTool[0] : preferredTools;
		this.damageConditions = damageConditions == null ? new DamageConditionCheck[0] : damageConditions;
		this.drops = drops == null ? new ItemDrop[0] : drops;
	}
	
	// for those with health, preferred tools, and drops a tile item.
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, PreferredTool preferredTool) {
		this(tileType, totalHealth, preferredTool, new ItemDrop(TileItem.get(tileType)));
	}
	// above, but custom drop
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, PreferredTool preferredTool, ItemDrop... drops) {
		this(tileType, totalHealth, preferredTool, (DamageConditionCheck)null, drops);
	}
	
	// for those that are one-shot, and have certain damage conditions, and drop a tile item
	DestructibleProperty(@NotNull TileType tileType, DamageConditionCheck... damageConditions) {
		this(tileType, new ItemDrop(TileItem.get(tileType)), damageConditions);
	}
	// above, but custom single item drop
	DestructibleProperty(@NotNull TileType tileType, ItemDrop drop, DamageConditionCheck... damageConditions) {
		this(tileType, 1, drop, damageConditions);
	}
	// single damage condition, multiple custom drops
	DestructibleProperty(@NotNull TileType tileType, DamageConditionCheck damageCondition, ItemDrop[] drops) {
		this(tileType, 1, null, damageCondition, drops);
	}
	
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable PreferredTool preferredTool, @Nullable DamageConditionCheck[] damageConditions, @Nullable ItemDrop... drops) {
		this(tileType, totalHealth, preferredTool == null ? null : new PreferredTool[] {preferredTool}, damageConditions, drops);
	}
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable PreferredTool preferredTool, @Nullable DamageConditionCheck damageCondition, @Nullable ItemDrop... drops) {
		this(tileType, totalHealth, preferredTool, damageCondition == null ? null : new DamageConditionCheck[] {damageCondition}, drops);
	}
	
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable ItemDrop drop, @Nullable PreferredTool[] preferredTools, @Nullable DamageConditionCheck... damageConditions) {
		this(tileType, totalHealth, preferredTools, damageConditions, drop == null ? null : new ItemDrop[] {drop});
	}
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable ItemDrop drop, @Nullable PreferredTool preferredTool, @Nullable DamageConditionCheck... damageConditions) {
		this(tileType, totalHealth, drop, preferredTool == null ? null : new PreferredTool[] {preferredTool}, damageConditions);
	}
	
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable ItemDrop drop, @Nullable DamageConditionCheck[] damageConditions, @Nullable PreferredTool... preferredTools) {
		this(tileType, totalHealth, preferredTools, damageConditions, drop == null ? null : new ItemDrop[] {drop});
	}
	DestructibleProperty(@NotNull TileType tileType, int totalHealth, @Nullable ItemDrop drop, @Nullable DamageConditionCheck damageCondition, @Nullable PreferredTool... preferredTools) {
		this(tileType, totalHealth, drop, preferredTools, damageCondition == null ? null : new DamageConditionCheck[] {damageCondition});
	}
	
	DestructibleProperty(DestructibleProperty model) {
		tileType = model.tileType;
		totalHealth = model.totalHealth;
		drops = model.drops;
		preferredTools = model.preferredTools;
		damageConditions = model.damageConditions;
	}
	
	
	boolean tileAttacked(@NotNull Tile tile, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			//if(tile.getServerLevel() != null)
			//	tile.getLevel().getWorld().getSender().sendData(new Hurt(attacker.getTag(), tile.getTag(), damage, Item.save(item)));
			
			// add damage particle
			tile.getLevel().addEntity(ActionParticle.ActionType.IMPACT.get(tile.getWorld(), null), tile.getCenter(), true);
			
			int health = totalHealth > 1 ? new Integer(tile.getData(TilePropertyType.Attack, tileType, HEALTH_IDX)) : 1;
			health -= damage;
			if(totalHealth > 1)
				tile.getLevel().addEntity(new TextParticle(tile.getWorld(), damage+""), tile.getCenter(), true);
			if(health <= 0) {
				tile.breakTile();
				dropItems(drops, tile, attacker);
			} else
				tile.setData(TilePropertyType.Attack, tileType, HEALTH_IDX, health+"");
			
			return true;
		}
		
		return false;
	}
	
	void dropItems(ItemDrop[] drops, @NotNull Tile tile, @NotNull WorldObject attacker) {}
	
	private int getDamage(@Nullable Item attackItem, int damage) {
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
