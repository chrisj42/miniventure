package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.TileItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.Material;
import miniventure.game.item.ToolType;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionParticle;
import miniventure.game.world.entity.particle.TextParticle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructibleProperty implements TileProperty {
	
	static final DestructibleProperty INDESTRUCTIBLE = new DestructibleProperty();
	
	private static final int HEALTH_IDX = 0;
	
	private final int totalHealth;
	
	private final PreferredTool preferredTool;
	
	private final DamageConditionCheck[] damageConditions;
	private ItemDrop[] drops;
	private boolean dropsTileItem = false;
	
	private TileType tileType;
	
	// indestructible constructor :3
	private DestructibleProperty() {
		totalHealth = -1;
		preferredTool = null;
		damageConditions = new DamageConditionCheck[] {(item) -> false};
		drops = new ItemDrop[0];
	}
	
	// this is for tiles with health
	DestructibleProperty(int totalHealth, @Nullable PreferredTool preferredTool, boolean dropsTileItem) {
		this(totalHealth, preferredTool, new ItemDrop[dropsTileItem?1:0]);
		this.dropsTileItem = dropsTileItem;
	}
	DestructibleProperty(int totalHealth, @Nullable PreferredTool preferredTool, ItemDrop... drops) {
		this.totalHealth = totalHealth;
		this.damageConditions = new DamageConditionCheck[0];
		this.drops = drops;
		
		this.preferredTool = preferredTool;
	}
	
	
	/// this is for tiles that are destroyed in one hit
	// NOTE, required tools count as damage conditions.
	DestructibleProperty(ItemDrop drop, DamageConditionCheck... damageConditions) {
		totalHealth = 1;
		preferredTool = null;
		this.damageConditions = damageConditions;
		this.drops = new ItemDrop[] {drop};
	}
	
	DestructibleProperty(boolean dropsTileItem, DamageConditionCheck... damageConditions) {
		this(null, damageConditions);
		this.dropsTileItem = dropsTileItem;
	}
	
	@Override
	public void init(@NotNull TileType type) {
		this.tileType = type;
		
		if(dropsTileItem)
			drops[0] = new ItemDrop(TileItem.get(type));
	}
	
	boolean tileAttacked(@NotNull Tile tile, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			// add damage particle
			tile.getLevel().addEntity(ActionParticle.ActionType.IMPACT.get(null), tile.getCenter(), true);
			
			int health = totalHealth > 1 ? new Integer(tile.getData(getClass(), tileType, HEALTH_IDX)) : 1;
			health -= damage;
			if(totalHealth > 1)
				tile.getLevel().addEntity(new TextParticle(damage+""), tile.getCenter(), true);
			if(health <= 0) {
				tile.breakTile();
				if(tile.getLevel() instanceof ServerLevel)
					for(ItemDrop drop: drops)
						if(drop != null)
							drop.dropItems((ServerLevel)tile.getLevel(), tile, attacker);
			} else
				tile.setData(getClass(), tileType, HEALTH_IDX, health+"");
			
			return true;
		}
		
		return false;
	}
	
	private int getDamage(@Nullable Item attackItem, int damage) {
		if(damageConditions.length > 0) {
			// must satisfy at least one condition
			boolean doDamage = true;
			for(DamageConditionCheck condition: damageConditions)
				doDamage = doDamage && condition.isDamagedBy(attackItem);
			
			if(!doDamage) return 0;
			// otherwise, continue.
		}
		
		if(preferredTool != null && attackItem instanceof ToolItem) {
			ToolType type = ((ToolItem)attackItem).getType();
			if(type == preferredTool.toolType)
				damage = (int) Math.ceil(damage * preferredTool.damageMultiplier);
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
			return (toolType == null || tool.getType() == toolType) && (material == null || tool.getMaterial() == material);
		}
	}
	
	@Override
	public Class<? extends TileProperty> getUniquePropertyClass() { return DestructibleProperty.class; }
	
	@Override
	public String toString() { return "DestructibleProperty[conditions="+damageConditions.length+"]"; }
}
