package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.entity.TextParticle;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

public enum FoodItem {
	
	Apple(2);
	
	private final int healthGained;
	
	FoodItem(int healthGained) {
		this.healthGained = healthGained;
	}
	
	@NotNull
	public Item get() {
		return new Item(name(), GameCore.icons.get(name().toLowerCase())) {
			@Override public Item copy() { return this; }
			
			@Override public void interact(Player player) {
				int gained = player.changeStat(Stat.Hunger, healthGained);
				if(gained > 0) {
					use();
					Level level = player.getLevel();
					if (level != null)
						level.addEntity(new TextParticle(gained + "", Color.CORAL), player.getCenter(), true);
				}
			}
		};
	}
}
