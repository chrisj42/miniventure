package miniventure.game.item.typenew;

@FunctionalInterface
public interface UsageBehavior extends ItemProperty {
	
	Item getUsedItem(Item item);
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return UsageBehavior.class; }
}
