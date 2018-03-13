package miniventure.game.item.typenew;

@FunctionalInterface
public interface StaminaUsageProperty extends ItemProperty {
	
	int getStaminaUsage(Item item);
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return StaminaUsageProperty.class; }
}
