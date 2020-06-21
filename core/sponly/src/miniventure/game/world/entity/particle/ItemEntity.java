package miniventure.game.world.entity.particle;

import miniventure.game.item.Item;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.Level;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemEntity extends Entity {
	
	private static final float PICKUP_DELAY = 0.5f;
	
	// private final LifetimeTracker lifetime;
	private final float spawnTime;
	private final BounceBehavior bounceBehavior;
	
	private final Item item;
	private final boolean delayPickup;
	
	public ItemEntity(@NotNull EntitySpawn info, Item item, @Nullable Vector2 goalDir) { this(info, item, goalDir, false); }
	public ItemEntity(@NotNull EntitySpawn info, Item item, @Nullable Vector2 goalDir, boolean delayPickup) {
		super(info);
		// this.lifetime = new LifetimeTracker(this, 180f);
		this.bounceBehavior = new BounceBehavior(this, goalDir);
		bounceBehavior.scaleVelocity(MathUtils.random(1f, 2f));
		spawnTime = info.getLevel().getWorld().getGameTime();
		this.item = item;
		this.delayPickup = delayPickup;
	}
	
	protected ItemEntity(@NotNull Level level, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(level, allData, version, modifier);
		SerialHashMap data = allData.get("item");
		delayPickup = false;
		item = Item.load(data.get("item"), version);
		bounceBehavior = new BounceBehavior(this, MyUtils.parseLayeredString(data.get("bounce")));
		spawnTime = level.getWorld().getGameTime();
		// lifetime = new LifetimeTracker(this, data.get("life", Float::parseFloat), bounceBehavior.getTime());
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		SerialHashMap data = new SerialHashMap();
		data.add("item", MyUtils.encodeStringArray(item.save()));
		data.add("bounce", MyUtils.encodeStringArray(bounceBehavior.save()));
		// data.add("life", lifetime.getLifetime());
		
		allData.put("item", data);
		return allData;
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		bounceBehavior.update(delta);
	}
	
	@Override
	protected TextureHolder getSprite() {
		return item.getTexture();
	}
	
	@Override
	public boolean touchedBy(Entity other) {
		if(other instanceof Player && getWorld().getGameTime() - spawnTime > PICKUP_DELAY * (delayPickup ? 4 : 1) && ((Player)other).takeItem(item)) {
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
