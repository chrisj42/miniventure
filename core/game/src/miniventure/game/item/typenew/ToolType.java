package miniventure.game.item.typenew;

import miniventure.game.api.Property;
import miniventure.game.api.PropertyFetcher;

public class ToolType implements PropertyFetcher<ItemProperty> {
	
	private static final float DURABILITY_BAR_HEIGHT = 4; // 8 pixels.
	
	public enum Material {
		Wood(30, 1, 5),
		
		Stone(80, 2, 5),
		
		Iron(250, 4, 4),
		
		Gem(800, 8, 3);
		
		public final int maxDurability; // the number of uses this level of tool gets.
		public final int damageMultiplier; // damage done by this tool is multiplied by this number.
		public final int staminaUsage; // the stamina points that are used up each use.
		
		Material(int maxDurability, int damageMultiplier, int staminaUsage) {
			this.maxDurability = maxDurability;
			this.damageMultiplier = damageMultiplier;
			this.staminaUsage = staminaUsage;
		}
	}
	
	
	public ToolType() {
		
	}
	
	@Override
	public ItemProperty[] getProperties() {
		return new ItemProperty[] {
			new UsageBehavior() {
				@Override
				public Item getUsedItem(Item item) {
					return null;
				}
			},
			
			(StaminaUsageProperty) (item) -> {
				
			},
			
			(StackableProperty) () -> 1
		};
	}
}
