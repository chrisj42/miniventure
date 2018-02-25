package miniventure.game.world;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldObject {
	
	Level getLevel();
	
	Rectangle getBounds();
	default Vector2 getCenter() { return getBounds().getCenter(new Vector2()); }
	default Vector2 getPosition() { return getBounds().getPosition(new Vector2()); }
	default Vector2 getSize() { return getBounds().getSize(new Vector2()); }
	
	default Rectangle getBounds(boolean worldOriginCenter) {
		Rectangle bounds = getBounds();
		if(worldOriginCenter) {
			bounds.x -= getLevel().getWidth()/2;
			bounds.y -= getLevel().getHeight()/2;
		}
		return bounds;
	}
	default Vector2 getCenter(boolean worldOriginCenter) { return getBounds(worldOriginCenter).getCenter(new Vector2()); }
	default Vector2 getPosition(boolean worldOriginCenter) { return getBounds(worldOriginCenter).getPosition(new Vector2()); }
	
	void update(float delta);
	
	void render(SpriteBatch batch, float delta, Vector2 posOffset);
	
	default float getLightRadius() { return 0; }
	
	// returns whether the given entity can share the same space as this object.
	boolean isPermeableBy(Entity entity);
	
	// returns whether interaction had any effect (should we look for other objects to interact with?)
	boolean interactWith(Player player, @Nullable Item heldItem);
	
	// returns whether attack had any effect (should we look for other objects to attack?)
	boolean attackedBy(WorldObject obj, @Nullable Item item, int dmg);
	
	boolean touchedBy(Entity entity);
	
	void touching(Entity entity);
	
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
