package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructibleProperty implements TileProperty {
	
	static final DestructibleProperty INDESTRUCTIBLE = new DestructibleProperty();
	
	private static final int HEALTH_IDX = 0;
	
	private final int totalHealth;
	
	//private final EnumMap<ToolType, Float> toolTypeDamageMultipliers = new EnumMap<>(ToolType.class);
	private final PreferredTool preferredTool;
	
	private final DamageConditionCheck[] damageConditions;
	private ItemDrop[] drops;
	
	private TileType tileType;
	
	// indestructible constructor :3
	private DestructibleProperty() {
		totalHealth = -1;
		preferredTool = null;
		damageConditions = new DamageConditionCheck[] {(item) -> false};
		drops = new ItemDrop[0];
	}
	
	// this is for tiles with health
	DestructibleProperty(int totalHealth, @Nullable PreferredTool preferredTool, ItemDrop... drops) {
		this.totalHealth = totalHealth;
		this.damageConditions = new DamageConditionCheck[0];
		this.drops = drops;
		
		this.preferredTool = preferredTool;
		//for(PreferredTool tool: preferredTools)
		//	toolTypeDamageMultipliers.put(tool.toolType, tool.damageMultiplier);
	}
	
	
	/// this is for tiles that are destroyed in one hit
	// NOTE, required tools count as damage conditions.
	DestructibleProperty(ItemDrop drop, DamageConditionCheck... damageConditions) {
		totalHealth = 1;
		preferredTool = null;
		this.damageConditions = damageConditions;
		this.drops = new ItemDrop[] {drop};
	}
	
	private boolean dropsTileItem = false;
	DestructibleProperty(boolean dropsTileItem, DamageConditionCheck... damageConditions) {
		this(null, damageConditions);
		this.dropsTileItem = dropsTileItem;
	}
	
	public void init(TileType type) {
		if(dropsTileItem)
			drops = new ItemDrop[] {new ItemDrop(new Item(TileItem.get(type)))};
		
		this.tileType = type;
	}
	
	boolean tileAttacked(Tile tile, Mob attacker, Item attackItem) {
		int damage = getDamage(tile, attackItem);
		return tileAttacked(tile, attacker, damage);
	}
		//System.out.println("attacked tile " + tile + " with " + attackItem + "; damage = " + damage);
	
	boolean tileAttacked(Tile tile, WorldObject attacker, int damage) {
		if(damage > 0) {
			int health = totalHealth > 1 ? tile.getData(getClass(), tileType, HEALTH_IDX) : 1;
			health -= damage;
			if(health <= 0) {
				for(ItemDrop drop: drops)
					if(drop != null)
						drop.dropItems(tile.getLevel(), tile, attacker);
				tile.resetTile(tile.getType().getProp(CoveredTileProperty.class).getCoveredTile(tile));
			} else
				tile.setData(getClass(), tileType, HEALTH_IDX, health);
			
			return true;
		}
		
		return false;
	}
	
	private int getDamage(@NotNull Tile attacked, @Nullable Item attackItem) {
		int damage = 1;
		if(attackItem != null)
			damage = attackItem.getDamage(attacked);
		
		if(damageConditions.length > 0) {
			// must satisfy at least one condition
			boolean doDamage = true;
			for(DamageConditionCheck condition: damageConditions)
				doDamage = doDamage && condition.isDamagedBy(attackItem);
			
			if(!doDamage) return 0;
			// otherwise, continue.
		}
		
		if(preferredTool != null && attackItem != null && attackItem.getItemData() instanceof ToolItem) {
			ToolType type = ((ToolItem)attackItem.getItemData()).getType();
			if(type == preferredTool.toolType)
				damage = (int) Math.ceil(damage * preferredTool.damageMultiplier);
			//if(toolTypeDamageMultipliers.containsKey(type))
			//	damage = (int) (damage * toolTypeDamageMultipliers.get(type));
		}
		
		return damage;
	}
	
	@Override
	public Integer[] getInitData() {
		if(totalHealth > 1) return new Integer[] {totalHealth};
		return new Integer[0]; // for a health of one or below, the tile will always be at max health, or destroyed.
	}
	
	
	
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
			return attackItem != null && attackItem.getItemData() instanceof ToolItem && ((ToolItem)attackItem.getItemData()).getType() == toolType;
		}
	}
	
	
	
	@Override
	public String toString() { return "DestructibleProperty[conditions="+damageConditions.length+"]"; }
}
