package miniventure.game.world.tile;

import miniventure.game.item.MaterialQuality;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.ArrayUtils;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.tile.TileDataTag.TileDataMap;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.particle.ParticleData.ActionParticleData;
import miniventure.game.world.entity.particle.ParticleData.TextParticleData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructionManager {
	
	static final DestructionManager INDESTRUCTIBLE(@NotNull TileTypeEnum tileType) {
		return new DestructibleBuilder(tileType, -1).require(item -> false).make();
	}
	
	private final TileTypeEnum tileType;
	private final int totalHealth;
	
	@NotNull private final PreferredTool[] preferredTools;
	@NotNull private final DamageConditionCheck[] damageConditions;
	@NotNull private final ItemDrop[] drops;
	
	// main constructor
	DestructionManager(@NotNull TileTypeEnum tileType, int totalHealth, @NotNull PreferredTool[] preferredTools, @NotNull DamageConditionCheck[] damageConditions, @NotNull ItemDrop[] drops) {
		this.tileType = tileType;
		this.totalHealth = totalHealth;
		this.preferredTools = preferredTools;
		this.damageConditions = damageConditions;
		this.drops = drops;
	}
	
	// for those with health, preferred tools, and drops an item.
	DestructionManager(@NotNull TileTypeEnum tileType, int totalHealth, PreferredTool preferredTool, ItemDrop... drops) {
		this(tileType, totalHealth, preferredTool == null ? new PreferredTool[0] : new PreferredTool[] {preferredTool}, new DamageConditionCheck[0], drops);
	}
	
	// for those that are one-shot, and have certain damage conditions, and drop an item
	DestructionManager(@NotNull TileTypeEnum tileType, @NotNull ItemDrop drop, DamageConditionCheck... damageConditions) {
		this(tileType, 1, new PreferredTool[0], damageConditions, new ItemDrop[] {drop});
	}
	
	DestructionManager(DestructionManager model) {
		this.tileType = model.tileType;
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
		public DestructibleBuilder(@NotNull TileTypeEnum type, int health) {
			this.type = type;
			this.health = health;
			preferredTools = new PreferredTool[0];
			damageConditions = new DamageConditionCheck[0];
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
				this.preferredTools = ArrayUtils.joinArrays(PreferredTool[].class, this.preferredTools, preferredTools);
			return this;
		}
		
		public DestructibleBuilder require(DamageConditionCheck... damageConditions) { return require(true, damageConditions); }
		public DestructibleBuilder require(boolean replace, DamageConditionCheck... damageConditions) {
			if(replace || this.damageConditions.length == 0)
				this.damageConditions = damageConditions;
			else if(damageConditions.length > 0)
				this.damageConditions = ArrayUtils.joinArrays(DamageConditionCheck[].class, this.damageConditions, damageConditions);
			return this;
		}
		
		public DestructibleBuilder drops(@NotNull ItemDrop... drops) { return drops(true, drops); }
		public DestructibleBuilder drops(boolean replace, @NotNull ItemDrop... drops) {
			if(replace || this.drops.length == 0)
				this.drops = drops;
			else if(drops.length > 0)
				this.drops = ArrayUtils.joinArrays(ItemDrop[].class, this.drops, drops);
			return this;
		}
		
		public DestructionManager make() {
			return new DestructionManager(type, health, preferredTools, damageConditions, drops);
		}
	}
	
	Result tileAttacked(@NotNull ServerTile tile, @NotNull WorldObject attacker, @Nullable ServerItem item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			//if(tile.getServerLevel() != null)
			//	tile.getLevel().getWorld().getSender().sendData(new Hurt(attacker.getTag(), tile.getTag(), damage, ServerItem.save(item)));
			
			// add damage particle
			tile.getServer().broadcastParticle(new ActionParticleData(ActionType.IMPACT), tile);
			
			// TileDataMap dataMap = tile.getDataMap(tileType);
			ServerLevel level = tile.getLevel();
			
			final int totalDamage = level.tileDamage.getOrDefault(tile, 0) + damage;
			final int health = totalHealth - totalDamage;
			if(totalHealth > 1)
				tile.getServer().broadcastParticle(new TextParticleData(String.valueOf(damage)), tile);
			if(health <= 0) {
				tile.getServer().playTileSound("break", tile, tileType);
				/*tile.getCacheMap(tileType).put(TileCacheTag.DestroyAction, () -> {
					for(ItemDrop drop: drops)
						tile.getLevel().dropItems(drop, tile, attacker);
				});*/
				for(ItemDrop drop: drops)
					tile.getLevel().dropItems(drop, tile, attacker);
				tile.breakTile();
			} else {
				level.tileDamage.put(tile, totalDamage);
				tile.getServer().playTileSound("hit", tile, tileType);
			}
			
			return damage > 1 || damageConditions.length > 0 ? Result.USED : Result.INTERACT;
		}
		
		return Result.NONE;
	}
	
	private int getDamage(@Nullable ServerItem attackItem, int damage) {
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
		boolean isDamagedBy(@Nullable ServerItem attackItem);
	}
	
	static class RequiredTool implements DamageConditionCheck {
		
		@Nullable private final ToolType toolType;
		@Nullable private final MaterialQuality material;
		
		public RequiredTool(@Nullable ToolType toolType) {
			this(toolType, null);
		}
		public RequiredTool(@Nullable ToolType toolType, @Nullable MaterialQuality material) {
			this.toolType = toolType;
			this.material = material;
		}
		
		@Override
		public boolean isDamagedBy(@Nullable ServerItem attackItem) {
			if(!(attackItem instanceof ToolItem))
				return false;
			
			ToolItem tool = (ToolItem) attackItem;
			return (toolType == null || tool.getToolType() == toolType) && (material == null || tool.getQuality() == material);
		}
	}
	
	@Override
	public String toString() { return "DestructibleProperty[conditions="+damageConditions.length+']'; }
}
