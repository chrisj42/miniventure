package miniventure.game.world.entity.mob.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.world.Point;
import miniventure.game.world.management.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

// controls how the client determines what to highlight
public enum CursorHighlight {
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
	
	
	public static final float MAX_CURSOR_RANGE = 3;
	
	public List<Tile> computeCursorPos(Vector2 center, Vector2 cursor, @NotNull Level level) {
		// final boolean print = Gdx.input.isButtonPressed(Buttos.LEFT);
		
		final float max = this == TILE_IN_RADIUS ? MAX_CURSOR_RANGE : this == INVISIBLE ? 0 : 1;
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
