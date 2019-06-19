package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;

import miniventure.game.network.PacketPipe;
import miniventure.game.world.entity.mob.Mob;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.esotericsoftware.kryonet.Connection;

import org.jetbrains.annotations.NotNull;

public interface Player extends Mob {
	
	int INV_SIZE = 50;
	int HOTBAR_SIZE = 5;
	float MOVE_SPEED = 5;
	float MAX_CURSOR_RANGE = 5;
	
	enum Stat {
		Health("heart", 10, 20),
		
		Stamina("bolt", 10, 50),
		
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
		bounds.height = Mob.unshortenSprite(bounds.height);
		Vector2 center = bounds.getCenter(new Vector2());
		Vector2 dir = getDirection().getVector();
		bounds.setSize(Math.abs(bounds.width*(1.5f*dir.x+1*dir.y)), Math.abs(bounds.height*(1*dir.x+1.5f*dir.y)));
		bounds.setCenter(center);
		bounds.x += dir.x*bounds.width*.75f;
		bounds.y += dir.y*bounds.height*.75f;
		return bounds;
	}
	
	void handlePlayerPackets(@NotNull Object packet, @NotNull PacketPipe.PacketPipeWriter packetSender);
}
