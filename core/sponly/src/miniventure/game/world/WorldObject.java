package miniventure.game.world;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldObject extends Boundable, Taggable<WorldObject> {
	
	@NotNull WorldManager getWorld();
	
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
}
