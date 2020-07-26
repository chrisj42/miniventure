package miniventure.game.world;

import miniventure.game.util.pool.RectPool;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Boundable {
	
	@Nullable Level getLevel();
	
	@NotNull Rectangle getBounds();
	
	default Vector2 getCenter() {
		Rectangle rect = getBounds();
		Vector2 v = rect.getCenter(new Vector2());
		RectPool.POOL.free(rect);
		return v;
	}
	default Vector2 getPosition() {
		Rectangle rect = getBounds();
		Vector2 v = rect.getPosition(new Vector2());
		RectPool.POOL.free(rect);
		return v;
	}
	default Vector2 getSize() {
		Rectangle rect = getBounds();
		Vector2 v = rect.getSize(new Vector2());
		RectPool.POOL.free(rect);
		return v;
	}
	
	// returns the closest tile to the center of this object, given an array of tiles.
	@Nullable
	default Tile getClosestTile(@NotNull Array<Tile> tiles) {
		if(tiles.size == 0) return null;
		
		Array<Tile> sorted = new Array<>(tiles);
		sortByDistance(sorted, getCenter());
		return sorted.get(0);
	}
	
	@Nullable
	default Tile getClosestTile() { return getClosestTile(false); }
	@Nullable
	default Tile getClosestTile(boolean clamp) {
		Level level = getLevel();
		if(level == null) return null;
		if(clamp)
			return level.getClosestTile(getCenter());
		else
			return level.getTile(getCenter());
	}
	
	/** @noinspection UnusedReturnValue*/
	static <E extends Boundable> Array<E> sortByDistance(@NotNull Array<E> objects, @NotNull Vector2 pos) {
		objects.sort((o1, o2) -> {
			float o1Diff = Math.abs(pos.dst(o1.getCenter()));
			float o2Diff = Math.abs(pos.dst(o2.getCenter()));
			return Float.compare(o1Diff, o2Diff);
		});
		
		return objects;
	}
	
}
