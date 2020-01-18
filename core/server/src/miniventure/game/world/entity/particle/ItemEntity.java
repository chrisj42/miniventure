package miniventure.game.world.entity.particle;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.item.ServerItem;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.ItemSpriteRenderer;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.management.ServerWorld;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemEntity extends ServerEntity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	private final LifetimeTracker lifetime;
	private final BounceBehavior bounceBehavior;
	
	private final ServerItem item;
	private final boolean delayPickup;
	
	public ItemEntity(@NotNull ServerWorld world, ServerItem item, @Nullable Vector2 goalDir) { this(world, item, goalDir, false); }
	public ItemEntity(@NotNull ServerWorld world, ServerItem item, @Nullable Vector2 goalDir, boolean delayPickup) {
		super(world);
		this.lifetime = new LifetimeTracker(this, 180f);
		this.bounceBehavior = new BounceBehavior(goalDir);
		bounceBehavior.scaleVelocity(MathUtils.random(1f, 2f));
		
		this.item = item;
		this.delayPickup = delayPickup;
		setRenderer(new ItemSpriteRenderer(item));
	}
	
	protected ItemEntity(@NotNull ServerWorld world, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(world, allData, version, modifier);
		SerialHashMap data = allData.get("item");
		delayPickup = false;
		item = ServerItem.load(MyUtils.parseLayeredString(data.get("item")), version);
		bounceBehavior = new BounceBehavior(MyUtils.parseLayeredString(data.get("bounce")));
		lifetime = new LifetimeTracker(this, data.get("life", Float::parseFloat), bounceBehavior.getTime());
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		SerialHashMap data = new SerialHashMap();
		data.add("item", MyUtils.encodeStringArray(item.save()));
		data.add("bounce", MyUtils.encodeStringArray(bounceBehavior.save()));
		data.add("life", lifetime.getLifetime());
		
		allData.put("item", data);
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
