package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.network.PacketPipe;
import miniventure.game.world.Point;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public interface Player extends Mob {
	
	int INV_SIZE = 50;
	int HOTBAR_SIZE = 5;
	float MOVE_SPEED = 5;
	float MAX_CURSOR_RANGE = 5;
	
	// controls how the client determines what to highlight
	enum CursorHighlight {
		INVISIBLE, TILE_IN_RADIUS, TILE_ADJACENT;
		
		public static final CursorHighlight[] values = CursorHighlight.values();
	}
	
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
	
	static List<Tile> traverseCursorRoute(Vector2 center, Vector2 edge, @NotNull Level level) {
		Vector2 dist = edge.cpy().sub(center);
		dist.setLength(Math.min(dist.len(), MAX_CURSOR_RANGE));
		edge.set(dist.add(center));
		
		dist.set(center.cpy().sub(edge));
		Tile prevTile = null;
		Vector2 prevPos = center.cpy();
		List<Tile> route = new LinkedList<>();
		while(true) {
			Vector2 pos = edge.cpy().add(dist);
			Tile tile = level.getClosestTile(pos);
			route.add(tile);
			
			if(!tile.getType().isWalkable())
				break;
			
			if(prevTile != null) {
				Point ppos = prevTile.getLocation();
				Point cpos = tile.getLocation();
				if(ppos.x != cpos.x && ppos.y != cpos.y) {
					// jumped a gap
					Vector2 midline = pos.cpy().sub(prevPos).scl(0.5f).add(prevPos);
					Tile otile1 = level.getTile(ppos.x, cpos.y);
					Tile otile2 = level.getTile(cpos.x, ppos.y);
					Vector2 ocenter1 = otile1.getCenter();
					Vector2 ocenter2 = otile2.getCenter();
					float dst1 = Vector2.dst(midline.x, midline.y, ocenter1.x, ocenter1.y);
					float dst2 = Vector2.dst(midline.x, midline.y, ocenter2.x, ocenter2.y);
					Tile fillTile = dst1 < dst2 ? otile1 : otile2;
					route.add(fillTile);
					if(!fillTile.getType().isWalkable())
						break;
				}
			}
			prevTile = tile;
			prevPos.set(pos);
			
			if(dist.len() == 0)
				break;
			
			dist.setLength(Math.max(0, dist.len() - 1));
		}
		
		return route;
	}
	
	default Tile getCursorTile(Vector2 cursorPos, @NotNull Level level) {
		Vector2 center = getCenter();
		List<Tile> cursorRoute = Player.traverseCursorRoute(center, cursorPos, level);
		// Vector2 dist = cursorPos.cpy().sub(center);
		Rectangle rect = getInteractionRect();
		if(cursorRoute.size() > 0)
			rect.setCenter(cursorRoute.get(cursorRoute.size()-1).getCenter());
		return level.getClosestTile(rect);
	}
}
