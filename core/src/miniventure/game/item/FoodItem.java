package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.entity.TextParticle;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.Stat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public enum FoodItem {
	
	Apple(2);
	
	private final int healthGained;
	
	FoodItem(int healthGained) {
		this.healthGained = healthGained;
	}
	
	@NotNull
	public Item get() {
		return new Item(name(), GameCore.icons.size() > 0 ? GameCore.icons.get(name().toLowerCase()) : new TextureRegion()) {
			@Override public Item copy() { return this; }
			
			@Override public void interact(Player player) {
				int gained = player.changeStat(Stat.Hunger, healthGained);
				Level level = player.getLevel();
				if(level != null)
					level.addEntity(new TextParticle(gained+"", Color.CORAL), player.getCenter(), true);
			}
		};
	}
}
