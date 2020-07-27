package miniventure.game.world;

import miniventure.game.util.pool.RectPool;
import miniventure.game.util.pool.VectorPool;
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
		Vector2 v = rect.getCenter(VectorPool.POOL.obtain());
		RectPool.POOL.free(rect);
		return v;
	}
	default Vector2 getPosition() {
		Rectangle rect = getBounds();
		Vector2 v = rect.getPosition(VectorPool.POOL.obtain());
		RectPool.POOL.free(rect);
		return v;
	}
	default Vector2 getSize() {
		Rectangle rect = getBounds();
		Vector2 v = rect.getSize(VectorPool.POOL.obtain());
		RectPool.POOL.free(rect);
		return v;
	}
	
	// returns the closest tile to the center of this object, given an array of tiles.
	@Nullable
	default Tile getClosestTile(@NotNull Array<Tile> tiles) {
		if(tiles.size == 0) return null;
		
		// Array<Tile> sorted = new Array<>(tiles);
		sortByDistance(tiles, getCenter(), true);
		return tiles.get(0);
	}
	
	@Nullable
	default Tile getClosestTile() { return getClosestTile(false); }
	@Nullable
	default Tile getClosestTile(boolean clamp) {
		Level level = getLevel();
		if(level == null) return null;
		if(clamp)
			return level.getClosestTile(getCenter(), true);
		else
			return level.getTile(getCenter(), true);
	}
	
	/** @noinspection UnusedReturnValue*/
	static <E extends Boundable> Array<E> sortByDistance(@NotNull Array<E> objects, @NotNull Vector2 pos) { return sortByDistance(objects, pos, false); }
	static <E extends Boundable> Array<E> sortByDistance(@NotNull Array<E> objects, @NotNull Vector2 pos, boolean free) {
		objects.sort((o1, o2) -> {
			Vector2 o1C = o1.getCenter();
			Vector2 o2C = o2.getCenter();
			float o1Diff = Math.abs(pos.dst(o1C));
			float o2Diff = Math.abs(pos.dst(o2C));
			VectorPool.POOL.free(o1C);
			VectorPool.POOL.free(o2C);
			return Float.compare(o1Diff, o2Diff);
		});
		
		if(free) VectorPool.POOL.free(pos);
		return objects;
	}
	
}
