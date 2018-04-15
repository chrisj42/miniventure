package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.item.Item;
import miniventure.game.util.Version;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.ItemSpriteRenderer;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class ItemEntity extends BounceEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final Item item;
	
	public ItemEntity(Item item, @Nullable Vector2 goalDir) {
		super(goalDir, 8f);
		this.item = item;
		setRenderer(new ItemSpriteRenderer(item));
	}
	
	protected ItemEntity(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
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
		if(other instanceof ServerPlayer && getTime() > PICKUP_DELAY && ((ServerPlayer)other).takeItem(item)) {
			remove();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void touching(Entity entity) { touchedBy(entity); }
}
