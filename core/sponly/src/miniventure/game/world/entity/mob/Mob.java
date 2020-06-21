package miniventure.game.world.entity.mob;

import miniventure.game.core.AudioCore.SoundEffect;
import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.entity.KnockbackController;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.particle.TextParticle;
import miniventure.game.world.management.Level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is necessary because it ought to nicely package up the functionality of a mob, that moves around, and has up/down/left/right walking animations. Though, I may move the directional + state driven animation to its own class...
 */
public abstract class Mob extends Entity {
	
	// for knockback, the whole process should take about 0.5s. The first half at a constant speed, and the second half can be spend slowing down at a linear pace.
	
	public static final float KNOCKBACK_SPEED = 10; // in tiles / second
	public static final float MIN_KNOCKBACK_TIME = 0.05f;
	public static final float MAX_KNOCKBACK_TIME = 0.25f;
	public static final float DAMAGE_PERCENT_FOR_MAX_PUSH = 0.2f;
	
	public static final float HURT_COOLDOWN = 0.25f; // minimum time between taking damage, in seconds; prevents a mob from getting hurt multiple times in quick succession.
	
	public static float shortenSprite(float height) { return height / 2; }
	public static float unshortenSprite(float height) { return height * 2; }
	
	public static float getKnockbackDuration(float healthPercent) { return MyUtils.mapFloat(Math.min(healthPercent, DAMAGE_PERCENT_FOR_MAX_PUSH), 0, DAMAGE_PERCENT_FOR_MAX_PUSH, MIN_KNOCKBACK_TIME, MAX_KNOCKBACK_TIME); }
	
	@NotNull private Direction dir;
	@NotNull private MobAnimationController animator;
	
	@NotNull private KnockbackController knockbackController;
	
	private float invulnerableTime = 0;
	
	private final String spriteName;
	
	protected Mob(@NotNull EntitySpawn info, String spriteName) {
		super(info);
		dir = Direction.DOWN;
		// this.maxHealth = health;
		// this.health = health;
		
		this.spriteName = spriteName;
		
		knockbackController = new KnockbackController(this);
		
		animator = new MobAnimationController(this, spriteName);
	}
	
	// some stuff is given in the child constructor; this shouldn't need to be saved to file.
	// these include the sprite name and the max health, in this case.
	// these things
	protected Mob(@NotNull Level level, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(level, allData, version, modifier);
		SerialHashMap data = allData.get("mob");
		
		this.spriteName = data.get("sprite");
		dir = data.get("dir", Direction::valueOf);
		// maxHealth = data.get("mhp", Integer::parseInt);
		// health = data.get("hp", Integer::parseInt);
		
		knockbackController = new KnockbackController(this);
		animator = new MobAnimationController(this, spriteName);
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		SerialHashMap data = new SerialHashMap();
		data.add("sprite", spriteName);
		data.add("dir", dir);
		// data.add("mhp", maxHealth);
		// data.add("hp", health);
		
		allData.put("mob", data);
		return allData;
	}
	
	public void reset() {
		dir = Direction.DOWN;
		knockbackController.reset();
		invulnerableTime = 0;
		animator.reset();
	}
	
	protected abstract int getMaxHealth();
	protected abstract int getHealth();
	protected abstract int changeHealth(int amount);
	// protected void setHealth(int health) { this.health = health; if(health == 0) die(); }
	
	// @Override
	public Direction getDirection() { return dir; }
	protected void setDirection(@NotNull Direction dir) {
		if(animator.setDirection(dir)) {
			this.dir = dir;
			// getServer().broadcastLocal(getLevel(), this, new MobUpdate(getTag(), dir));
		}
	}
	
	// @Override
	public boolean isKnockedBack() { return knockbackController.hasKnockback(); }
	
	@Override
	public void update(float delta) {
		animator.progressAnimation();
		
		// SpriteUpdate newSprite = animator.getSpriteUpdate();
		// if(newSprite != null)
		// 	updateSprite(newSprite);
		
		knockbackController.update(delta);
		
		super.update(delta);
		
		if(invulnerableTime > 0) invulnerableTime -= Math.min(invulnerableTime, delta);
	}
	
	@Override
	protected TextureHolder getSprite() {
		return animator.getSprite(getWorld().getGameTime());
	}
	
	@Override @NotNull
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.setHeight(shortenSprite(bounds.getHeight()));
		return bounds;
	}
	
	@Override
	public boolean move(float xd, float yd, float zd) {
		boolean moved = super.move(xd, yd, zd);
		
		if(xd != 0 || yd != 0) animator.requestState(AnimationState.WALK);
		
		Direction dir = Direction.getDirection(xd, yd);
		if(dir != null) {
			// set/change sprite direction
			setDirection(dir);
		}
		
		return moved;
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(invulnerableTime > 0) return Result.INTERACT; // consume the event, but not the item.
		
		boolean use = false;
		if(item instanceof ToolItem) {
			use = true;
			ToolItem ti = (ToolItem) item;
			if(ti.getToolType() == ToolType.Sword)
				damage *= 3;
			else if(ti.getToolType() == ToolType.Axe)
				damage *= 2;
			else
				use = false;
		}
		
		// health -= Math.min(damage, health);
		int change = changeHealth(-damage);
		invulnerableTime = HURT_COOLDOWN;
		
		// getServer().playEntitySound("hurt", this);
		(this instanceof Player ? SoundEffect.PLAYER_HURT : SoundEffect.ENTITY_HURT).play(this);
		
		if(getHealth() > 0) {
			// do knockback
			knockbackController.knock(obj, KNOCKBACK_SPEED, getKnockbackDuration(damage*1f/getMaxHealth()));
			setBlinker(Color.RED, Mob.HURT_COOLDOWN, true, new FrameBlinker(5, 1, false));
		}
		
		new TextParticle(EntitySpawn.get(level, obj.getCenter()), String.valueOf(damage), this instanceof Player ? Color.PINK : Color.RED);
		
		if (getHealth() == 0)
			die();
		
		return use ? Result.USED : Result.INTERACT;
	}
	
	// protected void regenHealth(int amount) { health = Math.min(maxHealth, health + amount); }
	
	public void die() { remove(); }
	
	// public boolean maySpawn() { return true; }
	/*public boolean maySpawn(TileType type) {
		return type == TileType.GRASS || type == TileType.DIRT || type == TileType.SAND || type == TileType.SNOW;
	}*/
}
