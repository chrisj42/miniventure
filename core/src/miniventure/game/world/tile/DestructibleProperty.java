package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructibleProperty implements TileProperty {
	
	static final DestructibleProperty INDESTRUCTIBLE = new DestructibleProperty();
	
	private static final int HEALTH_IDX = 0;
	
	private final TileType coveredTile;
	private final int totalHealth;
	
	//private final EnumMap<ToolType, Float> toolTypeDamageMultipliers = new EnumMap<>(ToolType.class);
	private final PreferredTool preferredTool;
	
	private final DamageConditionCheck[] damageConditions;
	private final ItemDrop[] drops;
	
	private DestructibleProperty() {
		coveredTile = null;
		totalHealth = -1;
		preferredTool = null;
		damageConditions = new DamageConditionCheck[] {(item) -> false};
		drops = new ItemDrop[0];
	}
	
	// this is for tiles with health
	public DestructibleProperty(int totalHealth, TileType coveredTile, @Nullable PreferredTool preferredTool, ItemDrop... drops) {
		this.coveredTile = coveredTile;
		this.totalHealth = totalHealth;
		this.damageConditions = new DamageConditionCheck[0];
		this.drops = drops;
		
		this.preferredTool = preferredTool;
		//for(PreferredTool tool: preferredTools)
		//	toolTypeDamageMultipliers.put(tool.toolType, tool.damageMultiplier);
	}
	
	// this is for tiles that are destroyed in one hit 
	public DestructibleProperty(TileType coveredTile, ItemDrop drop, DamageConditionCheck... damageConditions) {
		this.coveredTile = coveredTile;
		totalHealth = 1;
		preferredTool = null;
		this.damageConditions = damageConditions;
		this.drops = new ItemDrop[] {drop};
	}
	
	
	void tileAttacked(Tile tile, Item attackItem) {
		int damage = getDamage(attackItem, attackItem.getDamage());
		if(damage > 0) {
			int health = tile.getData(this, HEALTH_IDX);
			health -= damage;
			if(health <= 0)
				tile.resetTile(coveredTile);
		}
	}
	
	
	private int getDamage(@Nullable Item attackItem, int damage) {
		if(damageConditions.length > 0) {
			// must satisfy at least one condition
			boolean doDamage = false;
			for(DamageConditionCheck condition: damageConditions) {
				if(condition.isDamagedBy(attackItem)) {
					doDamage = true;
					break;
				}
			}
			
			if(!doDamage) return 0;
			// otherwise, continue.
		}
		
		if(preferredTool != null && attackItem instanceof ToolItem) {
			ToolType type = ((ToolItem)attackItem).getType();
			if(type == preferredTool.toolType)
				damage = (int) Math.ceil(damage * preferredTool.damageMultiplier);
			//if(toolTypeDamageMultipliers.containsKey(type))
			//	damage = (int) (damage * toolTypeDamageMultipliers.get(type));
		}
		
		return damage;
	}
	
	@Override
	public int getDataLength() {
		if(totalHealth > 1) return 1;
		return 0; // for a health of one or below, the tile will always be at max health, or destroyed.
	}
	
	public TileType getCoveredTile() { return coveredTile; }
	
	
	// TODO make methods for when tile is destroyed, and getting health of the tile. I'm not doing it now because I'm not sure yet how I'm going to implement it.
	
	
	static class PreferredTool {
		
		private final ToolType toolType;
		private final float damageMultiplier;
		
		public PreferredTool(@NotNull ToolType toolType, float damageMultiplier) {
			this.toolType = toolType;
			this.damageMultiplier = damageMultiplier;
		}
		
	}
	
	@FunctionalInterface
	interface DamageConditionCheck {
		boolean isDamagedBy(@Nullable Item attackItem);
	}
	
	static class RequiredTool implements DamageConditionCheck {
		
		private final ToolType toolType;
		
		public RequiredTool(ToolType toolType) {
			this.toolType = toolType;
		}
		
		@Override
		public boolean isDamagedBy(@Nullable Item attackItem) {
			return attackItem instanceof ToolItem && ((ToolItem)attackItem).getType() == toolType;
		}
	}
}
