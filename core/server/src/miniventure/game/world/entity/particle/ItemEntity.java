package miniventure.game.world.entity.particle;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.item.ServerItem;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.ItemSpriteRenderer;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public class ItemEntity extends ServerEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final LifetimeTracker lifetime;
	private final BounceBehavior bounceBehavior;
	
	private final ServerItem item;
	private final boolean delayPickup;
	
	public ItemEntity(ServerItem item, @Nullable Vector2 goalDir) { this(item, goalDir, false); }
	public ItemEntity(ServerItem item, @Nullable Vector2 goalDir, boolean delayPickup) {
		this.lifetime = new LifetimeTracker(this, 180f);
		this.bounceBehavior = new BounceBehavior(goalDir);
		bounceBehavior.scaleVelocity(MathUtils.random(1f, 2f));
		
		this.item = item;
		this.delayPickup = delayPickup;
		setRenderer(new ItemSpriteRenderer(item));
	}
	
	protected ItemEntity(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(1);
		delayPickup = false;
		item = ServerItem.load(MyUtils.parseLayeredString(data.get(0)));
		bounceBehavior = new BounceBehavior(MyUtils.parseLayeredString(data.get(1)));
		lifetime = new LifetimeTracker(this, Float.parseFloat(data.get(2)), bounceBehavior.getTime());
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			MyUtils.encodeStringArray(item.save()),
			MyUtils.encodeStringArray(bounceBehavior.save()),
			String.valueOf(lifetime.getLifetime())
		));
		
		allData.add(data);
		return allData;
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		bounceBehavior.update(this, delta);
		lifetime.update(delta);
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(other instanceof ServerPlayer && lifetime.getTime() > PICKUP_DELAY * (delayPickup ? 4 : 1) && ((ServerPlayer)other).takeItem(item)) {
			remove();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void touching(Entity entity) { touchedBy(entity); }
	
	@Override
	public boolean isPermeable() { return true; }
	
	@Override
	public String toString() { return super.toString()+"("+item+")"; }
}
