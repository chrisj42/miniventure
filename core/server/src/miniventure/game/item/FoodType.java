package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.SimpleEnumItem;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.Stat;

import org.jetbrains.annotations.NotNull;

public enum FoodType {
	
	Apple(3), Pear(3), Cherry(1), Strawberry(2),
	Snow_Berries(2), Gooseberry(2), Cactus_Fruit(4),
	Raw_Bacon(2), Cooked_Bacon(3), Raw_Meat(3), Cooked_Meat(6);
	
	private final int healthGained;
	
	FoodType(int healthGained) {
		this.healthGained = healthGained;
	}
	
	@NotNull
	public ServerItem get() { return new FoodItem(); }
	
	class FoodItem extends SimpleEnumItem {
		private FoodItem() {
			super(ItemType.Food, name(), GameCore.icons.get("items/food/"+name().toLowerCase()));
		}
		
		@Override public Result interact(Player player) {
			int gained = player.changeStat(Stat.Hunger, healthGained);
			if(gained > 0)
				return Result.USED;
			return Result.NONE;
		}
	}
}
