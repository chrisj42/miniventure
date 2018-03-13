package miniventure.game.world.entity.particle;

import miniventure.game.item.Item;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class ItemEntity extends BounceEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final Item item;
	private final TextureRegion texture;
	
	public ItemEntity(Item item, @NotNull Vector2 goalDir) {
		super(goalDir, 8f);
		this.item = item;
		texture = item.getTexture();
	}
	
	@Override
	protected TextureRegion getSprite() { return texture; }
	
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
