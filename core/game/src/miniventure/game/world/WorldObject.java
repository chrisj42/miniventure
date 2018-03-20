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

public interface WorldObject extends Boundable {
	
	@NotNull WorldManager getWorld();
	
	@Nullable ServerLevel getServerLevel();
	
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
	
	interface Tag {
		WorldObject getObject(WorldManager world);
	}
	
	Tag getTag();
}
