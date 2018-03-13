package miniventure.game.item.typenew;

@FunctionalInterface
public interface StackableProperty extends ItemProperty {
	
	int getMaxStackSize();
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return StackableProperty.class; }
}
