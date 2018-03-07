package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class ItemEntity extends BounceEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final Item item;
	
	public ItemEntity(Item item, @NotNull Vector2 goalDir) {
		super(item.getTexture(), goalDir,8f);
		this.item = item;
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(other instanceof Player && getTime() > PICKUP_DELAY && ((Player)other).takeItem(item)) {
			remove();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void touching(Entity entity) {
		touchedBy(entity);
	}
}
