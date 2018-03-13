package miniventure.game.item.type;

import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;
import miniventure.game.world.entity.particle.TextParticle;

import com.badlogic.gdx.graphics.Color;

public class FoodItem extends Item {
	
	private final int healthGained;
	
	FoodItem(String name, int healthGained) {
		super(name);
		this.healthGained = healthGained;
	}
	
	@Override public void interact(Player player) {
		int gained = player.changeStat(Stat.Hunger, healthGained);
		if(gained > 0) {
			use();
			Level level = player.getLevel();
			if (level != null)
				level.addEntity(new TextParticle(gained + "", Color.CORAL), player.getCenter(), true);
		}
	}
	
	@Override
	public Item copy() { return new FoodItem(getName(), healthGained); }
}
