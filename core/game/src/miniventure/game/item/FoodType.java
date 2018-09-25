package miniventure.game.item;

import miniventure.game.item.ItemType.SimpleEnumItem;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;

import org.jetbrains.annotations.NotNull;

public enum FoodType {
	
	Apple(2);
	
	private final int healthGained;
	
	FoodType(int healthGained) {
		this.healthGained = healthGained;
	}
	
	@NotNull
	public Item get() { return new FoodItem(); }
	
	class FoodItem extends SimpleEnumItem {
		private FoodItem() {
			super(ItemType.Food, name());
		}
		
		@Override public void interact(Player player) {
			int gained = player.changeStat(Stat.Hunger, healthGained);
			if(gained > 0) {
				use();
			}
		}
	}
}
