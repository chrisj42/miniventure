package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class ItemEntity extends BounceEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final Item item;
	
	public ItemEntity(Item item, @NotNull Vector2 goalDir) {
		super(new Sprite(item.getItemData().getTexture()), goalDir,8f);
		this.item = item;
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(other instanceof Player && getTime() > PICKUP_DELAY) {
			((Player)other).addToInventory(item);
			remove();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void touching(Entity entity) {
		touchedBy(entity);
	}
	
	@Override
	public boolean attackedBy(Mob mob, Item attackItem) { return false; }
}
