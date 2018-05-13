package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.item.Item;
import miniventure.game.util.Version;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.ItemSpriteRenderer;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class ItemEntity extends BounceEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final Item item;
	private final boolean delayPickup;
	
	public ItemEntity(Item item, @Nullable Vector2 goalDir) { this(item, goalDir, false); }
	public ItemEntity(Item item, @Nullable Vector2 goalDir, boolean delayPickup) {
		super(goalDir, 180f);
		scaleVelocity(MathUtils.random(1f, 2f));
		this.item = item;
		this.delayPickup = delayPickup;
		setRenderer(new ItemSpriteRenderer(item));
	}
	
	protected ItemEntity(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		delayPickup = false;
		item = Item.load(data);
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(item.save());
		
		return data;
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(other instanceof ServerPlayer && getTime() > PICKUP_DELAY * (delayPickup ? 4 : 1) && ((ServerPlayer)other).takeItem(item)) {
			remove();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void touching(Entity entity) { touchedBy(entity); }
	
	@Override
	public String toString() { return super.toString()+"("+item+")"; }
}
