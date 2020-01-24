package miniventure.game.world.entity.mob.player;

import java.util.ArrayList;
import java.util.Collections;
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
	
	// these are all measured in tiles
	float MOVE_SPEED = 5;
	float MAX_CURSOR_RANGE = 3;
	float INTERACT_RECT_SIZE = 1;
	
	// controls how the client determines what to highlight
	enum CursorHighlight {
		// no cursor; item likely does not interact with the world. If it can be used, it's probably something the player uses on themselves
		INVISIBLE,
		
		// cursor on the tile in front of the player; used for things which expect to interact with a tile
		TILE_ADJACENT,
		
		// cursor in the area in front of the player; used for things that expect to interact with entities, not a particular tile
		FRONT_AREA,
		
		// cursor on a tile that can be a few tiles away; used for placeable objects
		// will generally also show a custom sprite at the cursor
		TILE_IN_RADIUS;
		
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
	
	default Rectangle getInteractionRect(Vector2 center) {
		if(center == null)
			return new Rectangle(-1, -1, 0 ,0);
		
		Rectangle bounds = new Rectangle();
		bounds.setSize(INTERACT_RECT_SIZE);
		bounds.setCenter(center);
		return bounds;
	}
	
	void handlePlayerPackets(@NotNull Object packet, @NotNull PacketPipe.PacketPipeWriter packetSender);
	
	static List<Tile> computeCursorPos(Vector2 center, Vector2 cursor, @NotNull Level level, CursorHighlight highlightMode) {
		// final boolean print = Gdx.input.isButtonPressed(Buttos.LEFT);
		
		final float max = highlightMode == CursorHighlight.TILE_IN_RADIUS ? MAX_CURSOR_RANGE : highlightMode == CursorHighlight.INVISIBLE ? 0 : 1;
		// get vector to target pos
		Vector2 dist = cursor.cpy().sub(center).clamp(0, max);
		cursor.set(center).add(dist);
		
		// determine the closest
		final Point startTile = Point.floorVector(center);
		final Point cursorTile = Point.floorVector(cursor);
		
		// first, check if we can take shortcuts
		if(startTile.equals(cursorTile))
			return new LinkedList<>(Collections.singletonList(level.getTile(center))); // it's within the same tile, so no checks are needed.
		
		// this was originally going to account for the fact that tile coordinates are the bottom right corner, but it seems I didn't need that to make it work well.
		// final int startOffX = startTile.x < cursorTile.x ? 1 : 0;
		// final int startOffY = startTile.y < cursorTile.y ? 1 : 0;
		// final int endOffX = cursorTile.x < startTile.x ? 1 : 0;
		// final int endOffY = cursorTile.y < startTile.y ? 1 : 0;
		
		final int xDelta = cursorTile.x - startTile.x;
		final int yDelta = cursorTile.y - startTile.y;
		final int xDiff = Math.abs(xDelta);
		final int yDiff = Math.abs(yDelta);
		
		final int xdir = xDelta < 0 ? -1 : 1;
		final int ydir = yDelta < 0 ? -1 : 1;
		
		final boolean useX = xDiff > yDiff;
		
		LinkedList<Point> path = new LinkedList<>();
		LinkedList<Point> altPath = new LinkedList<>();
		
		int curX = startTile.x;
		int curY = startTile.y;
		
		final float accumDelta = useX ? yDiff / (float) xDiff : xDiff / (float) yDiff;
		float accumPos = (useX?yDiff:xDiff) > 0 && accumDelta >= .5f ? 1 - accumDelta : 0;
		
		for(int i = 0; i <= (useX ? xDiff : yDiff); i++) {
			path.add(new Point(curX, curY));
			
			if(accumPos >= 1) {
				accumPos -= 1;
				if(useX) curY += ydir;
				else curX += xdir;
				path.add(new Point(curX, curY));
				
				if(useX) altPath.add(new Point(curX-xdir, curY));
				else altPath.add(new Point(curX, curY-ydir));
			}
			
			altPath.add(new Point(curX, curY));
			
			accumPos += accumDelta;
			if(useX) curX += xdir;
			else curY += ydir;
		}
		
		List<Tile> tiles = new ArrayList<>(path.size());
		boolean blocked = false;
		while(path.size() > 0) {
			Point loc = path.removeFirst();
			Tile tile = level.getTile(loc.x, loc.y);
			if(!tile.isPermeable()) {
				Point altLoc = altPath.removeFirst();
				Tile altTile = level.getTile(altLoc.x, altLoc.y);
				if(blocked) {
					tiles.add(altTile);
					tiles.add(tile);
				}
				else if(altTile.isPermeable()) {
					tiles.add(altTile);
				} else {
					tiles.add(tile);
					// restrict cursor to this tile
					cursor.set(tile.getCenter());
					blocked = true;
				}
			} else {
				altPath.removeFirst();
				tiles.add(tile); 
			}
		}
		return tiles;
	}
}
