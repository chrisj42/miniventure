package miniventure.game.item.typenew;

import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;
import miniventure.game.world.entity.particle.TextParticle;

import com.badlogic.gdx.graphics.Color;

public class EdibleProperty implements ReflexiveUseProperty {
	
	private final int healthGained;
	
	public EdibleProperty(int healthGained) {
		this.healthGained = healthGained;
	}
	
	@Override
	public void interact(Player player, Item item) {
		int gained = player.changeStat(Stat.Hunger, healthGained);
		if(gained > 0) {
			item.use();
			Level level = player.getLevel();
			if (level != null)
				level.addEntity(new TextParticle(gained + "", Color.CORAL), player.getCenter(), true);
		}
	}
}
