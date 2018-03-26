package miniventure.game.world.entity.mob;

import java.util.Arrays;

import miniventure.game.GameProtocol.EntityUpdate;
import miniventure.game.GameProtocol.Hurt;
import miniventure.game.GameProtocol.MobUpdate;
import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.item.Item;
import miniventure.game.item.ToolItem;
import miniventure.game.item.ToolType;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.KnockbackController;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;
import miniventure.game.world.entity.particle.TextParticle;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is necessary because it ought to nicely package up the functionality of a mob, that moves around, and has up/down/left/right walking animations. Though, I may move the directional + state driven animation to its own class...
 */
public abstract class ServerMob extends ServerEntity implements Mob {
	
	private static final float HURT_COOLDOWN = 0.5f; // minimum time between taking damage, in seconds; prevents a mob from getting hurt multiple times in quick succession. 
	
	@NotNull private Direction dir;
	@NotNull private MobAnimationController animator;
	
	private final int maxHealth;
	private int health;
	
	@NotNull private KnockbackController knockbackController;
	//private float knockbackTimeLeft = 0;
	//@NotNull private Vector2 knockbackVelocity = new Vector2();
	
	private float invulnerableTime = 0;
	//private FrameBlinker blinker;
	
	private final String spriteName;
	
	public ServerMob(@NotNull String spriteName, int health) {
		super();
		dir = Direction.DOWN;
		this.maxHealth = health;
		this.health = health;
		
		this.spriteName = spriteName;
		
		knockbackController = new KnockbackController(this);
		
		animator = new MobAnimationController<>(this, spriteName);
		
		//setRenderer(animator.getRendererUpdate());
	}
	
	protected ServerMob(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		
		this.spriteName = data[0];
		dir = Direction.valueOf(data[1]);
		maxHealth = Integer.parseInt(data[2]);
		health = Integer.parseInt(data[3]);
		invulnerableTime = Float.parseFloat(data[4]);
		
		knockbackController = new KnockbackController(this);
		animator = new MobAnimationController<>(this, spriteName);
		
		//setRenderer(animator.getRendererUpdate());
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			spriteName,
			dir.name(),
			maxHealth+"",
			health+"",
			invulnerableTime+""
		});
		
		return data;
	}
	
	public void reset() {
		dir = Direction.DOWN;
		this.health = maxHealth;
		knockbackController.reset();
		invulnerableTime = 0;
		animator.progressAnimation(0);
	}
	
	protected int getHealth() { return health; }
	protected void setHealth(int health) { this.health = health; if(health == 0) remove(); }
	
	@Override
	public Direction getDirection() { return dir; }
	//protected void setDirection(@NotNull Direction dir) { this.dir = dir; }
	
	@Override
	public boolean isKnockedBack() { return knockbackController.hasKnockback(); }
	
	//@Override
	//protected TextureRegion getSprite() { return animator.getSprite(); }
	
	/*@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		blinker.update(delta);
		
		if(invulnerableTime <= 0 || blinker.shouldRender())
			super.render(batch, delta, posOffset);
	}*/
	
	@Override
	public void update(float delta) {
		animator.progressAnimation(delta);
		
		SpriteUpdate newSprite = animator.getSpriteUpdate();
		if(newSprite != null)
			updateSprite(newSprite);
		
		/*if(knockbackTimeLeft > 0) {
			super.move(new Vector2(knockbackVelocity).scl(delta));
			knockbackTimeLeft -= delta;
			if(knockbackTimeLeft <= 0) {
				knockbackTimeLeft = 0;
				knockbackVelocity.setZero();
			}
		}*/
		knockbackController.update(delta);
		
		super.update(delta);
		
		if(invulnerableTime > 0) invulnerableTime -= Math.min(invulnerableTime, delta);
	}
	
	@Override @NotNull
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.setHeight(bounds.getHeight()*2/3);
		return bounds;
	}
	
	@Override
	public boolean move(float xd, float yd, float zd) {
		boolean moved = super.move(xd, yd, zd);
		
		if(xd != 0 || yd != 0) animator.requestState(AnimationState.WALK);
		
		Direction dir = Direction.getDirection(xd, yd);
		if(dir != null) {
			// change sprite direction
			this.dir = dir;
			animator.setDirection(dir);
			ServerCore.getServer().broadcast(new MobUpdate(getTag(), dir), this);
		}
		
		return moved;
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(invulnerableTime > 0) return false; // this ought to return false because returning true would use up weapons and such, and that's not fair. Downside is, it'll then try to interact with other things...
		
		if(item instanceof ToolItem) {
			ToolItem ti = (ToolItem) item;
			if(ti.getToolType() == ToolType.Sword)
				damage *= 3;
			if(ti.getToolType() == ToolType.Axe)
				damage *= 2;
		}
		
		health -= Math.min(damage, health);
		invulnerableTime = HURT_COOLDOWN;
		
		if(health > 0) {
			// do knockback
			if(!(this instanceof Player)) // client will take care of it for themself
				knockbackController.knock(obj, KNOCKBACK_SPEED, Mob.getKnockbackDuration(damage*1f/maxHealth));
		}
		
		ServerLevel level = getLevel();
		if(level != null) {
			ServerCore.getServer().broadcast(new Hurt(obj.getTag(), getTag(), damage*1f/maxHealth));
			level.addEntity(new TextParticle(damage + "", this instanceof ServerPlayer ? Color.PINK : Color.RED), getCenter(), true);
		}
		
		if (health == 0)
			remove();
		
		return true;
	}
	
	void regenHealth(int amount) { health = Math.min(maxHealth, health + amount); }
	
	public boolean maySpawn(TileType type) {
		return type == TileType.GRASS || type == TileType.DIRT || type == TileType.SAND;
	}
}
