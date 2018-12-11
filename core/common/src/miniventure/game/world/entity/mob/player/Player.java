package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;

import miniventure.game.world.entity.mob.Mob;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public interface Player extends Mob {
	
	int INV_SIZE = 50;
	int HOTBAR_SIZE = 5;
	float MOVE_SPEED = 5;
	
	enum Stat {
		Health("heart", 10, 20),
		
		Stamina("bolt", 12, 100),
		
		Hunger("burger", 10, 20),
		
		Armor("", 10, 10, 0);
		
		public final int max, initial;
		final int iconCount;
		final String icon, outlineIcon;
		
		Stat(String icon, int iconCount, int max) { this(icon, iconCount, max, max); }
		Stat(String icon, int iconCount, int max, int initial) {
			this.max = max;
			this.initial = initial;
			this.icon = icon;
			this.outlineIcon = icon+"-outline";
			this.iconCount = iconCount;
		}
		
		public static final Stat[] values = Stat.values();
		
		static Integer[] save(EnumMap<Stat, Integer> stats) {
			Integer[] statValues = new Integer[Stat.values.length];
			for(Stat stat: stats.keySet())
				statValues[stat.ordinal()] = stats.get(stat);
			
			return statValues;
		}
		
		static void load(Integer[] data, EnumMap<Stat, Integer> stats) {
			for(int i = 0; i < data.length; i++) {
				if(data[i] == null) continue;
				Stat stat = Stat.values[i];
				stats.put(stat, data[i]);
			}
		}
	}
	
	Integer[] saveStats();
	int getStat(@NotNull Stat stat);
	int changeStat(@NotNull Stat stat, int amt);
	
	default Rectangle getInteractionRect() {
		Rectangle bounds = getBounds();
		Vector2 dirVector = getDirection().getVector();
		bounds.x += dirVector.x*7/10;
		bounds.y += dirVector.y*7/10;
		return bounds;
	}
}
