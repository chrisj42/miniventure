package miniventure.game.item;

import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;
import miniventure.game.world.entity.mob.player.Player.Stat;
import miniventure.game.world.entity.mob.player.ServerPlayer;

import org.jetbrains.annotations.NotNull;

public enum FoodType implements ServerItemSource {
	
	Apple(3), Pear(3), Cherry(1), Strawberry(2),
	Snow_Berries(2), Gooseberry(2), Cactus_Fruit(4),
	Raw_Bacon(2), Cooked_Bacon(3), Raw_Meat(3), Cooked_Meat(6);
	
	private final int healthGained;
	private final ServerItem item;
	
	FoodType(int healthGained) {
		this.healthGained = healthGained;
		item = new FoodItem();
	}
	
	@Override @NotNull
	public ServerItem get() { return item; }
	
	class FoodItem extends EnumItem {
		private FoodItem() {
			super(ItemType.Food, FoodType.this);
		}
		
		@Override @NotNull
		public Player.CursorHighlight getHighlightMode() {
			return CursorHighlight.INVISIBLE;
		}
		
		@Override
		public Result interact(ServerPlayer player) {
			int gained = player.changeStat(Stat.Hunger, healthGained);
			if(gained > 0)
				return Result.USED;
			return Result.NONE;
		}
	}
}
