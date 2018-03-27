package miniventure.game.world;

import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Boundable {
	
	@Nullable Level getLevel();
	
	@NotNull Rectangle getBounds();
	
	default Vector2 getCenter() { return getBounds().getCenter(new Vector2()); }
	default Vector2 getPosition() { return getBounds().getPosition(new Vector2()); }
	default Vector2 getSize() { return getBounds().getSize(new Vector2()); }
	
	static Vector2 toLevelCoords(@Nullable Level level, Vector3 pos) { return toLevelCoords(level, new Vector2(pos.x, pos.y)); }
	static Vector2 toLevelCoords(@Nullable Level level, Vector2 pos) {
		if(level != null) {
			pos.x -= level.getWidth() / 2;
			pos.y -= level.getHeight() / 2;
		}
		return pos;
	}
	
	default Rectangle getBounds(boolean worldOriginCenter) {
		Rectangle bounds = getBounds();
		if(worldOriginCenter) {
			Vector2 pos = toLevelCoords(getLevel(), new Vector2(bounds.x, bounds.y));
			bounds.setPosition(pos);
		}
		return bounds;
	}
	default Vector2 getCenter(boolean worldOriginCenter) { return getBounds(worldOriginCenter).getCenter(new Vector2()); }
	default Vector2 getPosition(boolean worldOriginCenter) { return getBounds(worldOriginCenter).getPosition(new Vector2()); }
	
	// returns the closest tile to the center of this object, given an array of tiles.
	@Nullable
	default Tile getClosestTile(@NotNull Array<Tile> tiles) {
		if(tiles.size == 0) return null;
		
		Array<Tile> sorted = new Array<>(tiles);
		sortByDistance(sorted, getCenter());
		return sorted.get(0);
	}
	
	/** @noinspection UnusedReturnValue*/
	static <E extends WorldObject> Array<E> sortByDistance(@NotNull Array<E> objects, @NotNull Vector2 pos) {
		objects.sort((o1, o2) -> {
			float o1Diff = Math.abs(pos.dst(o1.getCenter()));
			float o2Diff = Math.abs(pos.dst(o2.getCenter()));
			return Float.compare(o1Diff, o2Diff);
		});
		
		return objects;
	}
	
}
