package miniventure.game.world;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldObject {
	
	@NotNull
	default WorldManager getWorld() {
		return getLevel().getWorld();
	}
	
	@NotNull Level getLevel();
	
	@NotNull Rectangle getBounds();
	
	// a common Vector2 instance used by the bound fetchers to reduce object creation
	// Vector2 dummyVector();
	
	// TODO optimize creation of unnecessary short-lived Vector2 instances (same goes for getBounds() rectangle)
	default Vector2 getCenter() { return getCenter(new Vector2()); }
	default Vector2 getCenter(Vector2 v) { return getBounds().getCenter(v); }
	default Vector2 getPosition() { return getPosition(new Vector2()); }
	default Vector2 getPosition(Vector2 v) { return getBounds().getPosition(v); }
	default Vector2 getSize() { return getSize(new Vector2()); }
	default Vector2 getSize(Vector2 v) { return getBounds().getSize(v); }
	
	default float distance(WorldObject other) { return distance(this, other); }
	static float distance(WorldObject o1, WorldObject o2) {
		Vector2 v1 = MyUtils.getV2();
		Vector2 v2 = MyUtils.getV2();
		float dist = o1.getPosition(v1).dst(o2.getPosition(v2));
		MyUtils.freeV2(v1);
		MyUtils.freeV2(v2);
		return dist;
	}
	
	//void update(float delta);
	
	void render(SpriteBatch batch, float delta, Vector2 posOffset);
	
	default float getLightRadius() { return 0; }
	
	// returns whether this object is collision-enabled.
	boolean isPermeable();
	
	// returns whether interaction had any effect (should we look for other objects to interact with?)
	Result interactWith(Player player, @Nullable Item heldItem);
	
	// returns whether attack had any effect (should we look for other objects to attack?)
	Result attackedBy(WorldObject obj, @Nullable Item item, int dmg);
	
	boolean touchedBy(Entity entity);
	
	void touching(Entity entity);
	
	
	// returns the closest tile to the center of this object, given an array of tiles.
	@Nullable
	default Tile getClosestTile(@NotNull Array<Tile> tiles) {
		if(tiles.size == 0) return null;
		
		Array<Tile> sorted = new Array<>(tiles);
		Vector2 v = MyUtils.getV2();
		sortByDistance(sorted, getCenter(v));
		MyUtils.freeV2(v);
		return sorted.get(0);
	}
	
	@NotNull
	default Tile getClosestTile() {
		Vector2 v = MyUtils.getV2();
		Tile t = getLevel().getClosestTile(getCenter());
		MyUtils.freeV2(v);
		return t;
	}
	
	/** @noinspection UnusedReturnValue*/
	static <E extends WorldObject> Array<E> sortByDistance(@NotNull Array<E> objects, @NotNull Vector2 pos) {
		Vector2 v = MyUtils.getV2();
		objects.sort((o1, o2) -> {
			float o1Diff = Math.abs(pos.dst(o1.getCenter(v)));
			float o2Diff = Math.abs(pos.dst(o2.getCenter(v)));
			return Float.compare(o1Diff, o2Diff);
		});
		MyUtils.freeV2(v);
		
		return objects;
	}
}
