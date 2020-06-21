package miniventure.game.world.tile;

import miniventure.game.core.AudioCore.SoundEffect;
import miniventure.game.item.Item;
import miniventure.game.item.MaterialQuality;
import miniventure.game.item.Result;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.util.ArrayUtils;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.entity.particle.ActionParticle.ActionType;
import miniventure.game.world.entity.particle.TextParticle;
import miniventure.game.world.management.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DestructionManager implements TileProperty {
	
	static final DestructionManager INDESTRUCTIBLE = new DestructibleBuilder(-1).require(item -> false).make();
	
	private final int totalHealth;
	
	@NotNull private final PreferredTool[] preferredTools;
	@NotNull private final DamageConditionCheck[] damageConditions;
	@NotNull private final ItemDrop[] drops;
	
	// main constructor
	DestructionManager(int totalHealth, @NotNull PreferredTool[] preferredTools, @NotNull DamageConditionCheck[] damageConditions, @NotNull ItemDrop[] drops) {
		this.totalHealth = totalHealth;
		this.preferredTools = preferredTools;
		this.damageConditions = damageConditions;
		this.drops = drops;
	}
	
	// for those with health, preferred tools, and drops an item.
	DestructionManager(int totalHealth, PreferredTool preferredTool, ItemDrop... drops) {
		this(totalHealth, preferredTool == null ? new PreferredTool[0] : new PreferredTool[] {preferredTool}, new DamageConditionCheck[0], drops);
	}
	
	// for those that are one-shot, and have certain damage conditions, and drop an item
	DestructionManager(@NotNull ItemDrop drop, DamageConditionCheck... damageConditions) {
		this(1, new PreferredTool[0], damageConditions, new ItemDrop[] {drop});
	}
	
	DestructionManager(DestructionManager model) {
		totalHealth = model.totalHealth;
		drops = model.drops;
		preferredTools = model.preferredTools;
		damageConditions = model.damageConditions;
	}
	
	@Override
	public void registerDataTags(TileType tileType) {
		if(totalHealth > 1)
			tileType.addDataTag(TileDataTag.Health);
	}
	
	public static class DestructibleBuilder {
		private int health;
		private PreferredTool[] preferredTools;
		private DamageConditionCheck[] damageConditions;
		private ItemDrop[] drops;
		
		public DestructibleBuilder() { this(1); }
		public DestructibleBuilder(int health) {
			this.health = health;
			preferredTools = new PreferredTool[0];
			damageConditions = new DamageConditionCheck[0];
			drops = new ItemDrop[0];
		}
		
		public DestructibleBuilder(DestructionManager model) {
			this.health = model.totalHealth;
			this.drops = model.drops;
			this.preferredTools = model.preferredTools;
			this.damageConditions = model.damageConditions;
		}
		
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
			return new DestructionManager(health, preferredTools, damageConditions, drops);
		}
	}
	
	void tileDestroyed(@NotNull Tile.TileContext context) {
		for(ItemDrop drop: drops)
			context.<Level>getLevel().dropItems(drop, context.getTile(), null);
	}
	
	Result tileAttacked(@NotNull Tile.TileContext context, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			//if(tile.getLevel() != null)
			//	tile.getLevel().getWorld().getSender().sendData(new Hurt(attacker.getTag(), tile.getTag(), damage, Item.save(item)));
			
			// GameServer server = context.<WorldManager>getWorld().getServer();
			Tile tile = context.getTile();
			Level level = context.getLevel();
			
			// add damage particle
			// server.broadcastParticle(new ActionParticleData(ActionType.IMPACT), tile);
			ActionType.IMPACT.makeParticle(EntitySpawn.get(level, tile.getCenter()), null);
			
			int health = totalHealth > 1 ? context.getData(TileDataTag.Health, totalHealth) : 1;
			health -= damage;
			if(totalHealth > 1)
				new TextParticle(level.getSpawn(tile.getCenter()), String.valueOf(damage));
			if(health <= 0) {
				SoundEffect.TILE_BREAK.play(tile);
				tile.breakTile();
			} else {
				context.setData(TileDataTag.Health, health);
				SoundEffect.TILE_HIT.play(tile);
			}
			
			return damage > 1 || damageConditions.length > 0 ? Result.USED : Result.INTERACT;
		}
		
		return Result.NONE;
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
		boolean isDamagedBy(@Nullable Item attackItem);
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
		public boolean isDamagedBy(@Nullable Item attackItem) {
			if(!(attackItem instanceof ToolItem))
				return false;
			
			ToolItem tool = (ToolItem) attackItem;
			return (toolType == null || tool.getToolType() == toolType) && (material == null || tool.getQuality() == material);
		}
	}
	
	@Override
	public String toString() { return "DestructibleProperty[conditions="+damageConditions.length+']'; }
}
