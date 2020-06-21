package miniventure.game.item;

import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.world.entity.mob.player.CursorHighlight;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.NotNull;

public enum FoodType implements ItemEnum {
	
	Apple(3), Pear(3), Cherry(1), Strawberry(2),
	Snow_Berries(2), Gooseberry(2), Cactus_Fruit(4),
	Raw_Bacon(2), Cooked_Bacon(3), Raw_Meat(3), Cooked_Meat(6);
	
	private final int healthGained;
	private final Item item;
	
	FoodType(int healthGained) {
		this.healthGained = healthGained;
		item = new FoodItem();
	}
	
	@Override @NotNull
	public Item get() { return item; }
	
	class FoodItem extends EnumItem {
		private FoodItem() {
			super(ItemType.Food, FoodType.this);
		}
		
		@Override @NotNull
		public CursorHighlight getHighlightMode() {
			return CursorHighlight.INVISIBLE;
		}
		
		@Override public Result interact(Player player) {
			int gained = player.restoreHunger(healthGained);
			if(gained > 0)
				return Result.USED;
			return Result.NONE;
		}
	}
}
