package miniventure.game.item.typenew;

import miniventure.game.GameCore;
import miniventure.game.api.Property;
import miniventure.game.api.PropertyFetcher;

public interface ItemProperty extends Property<ItemProperty> {
	
	static PropertyFetcher<ItemProperty> getDefaultPropertyFetcher() {
		return () -> new ItemProperty[] {
				(StackableProperty) () -> 64,
				(StaminaUsageProperty) (item) -> 1,
				(AttackProperty) (obj, player, item) -> obj.attackedBy(player, item, 1),
				(InteractProperty) (obj, player, item) -> obj.interactWith(player, item),
				(ReflexiveUseProperty) (player, item) -> {},
				(UsageBehavior) (item) -> null,
				(RenderProperty) (item) -> GameCore.icons.get(item.getType().name().toLowerCase())
		};
	}
	
	@Override
	default String[] getInitialData() { return new String[0]; }
	
	
}
