package miniventure.game.world.entity.mob;

import miniventure.game.world.ItemDrop;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

/**
 * This class is necessary because it ought to nicely package up the functionality of a mob, that moves around, and has up/down/left/right walking animations. Though, I may move the directional + state driven animation to its own class...
 */
public abstract class Mob extends Entity {
	
	private static final float SKID_FRICTION = 1; // this is the acceleration, in units / second, that goes against and slows your knockback velocity.
	
	@NotNull private Direction dir;
	@NotNull private MobAnimationController animator;
	
	private int health;
	@NotNull private ItemDrop[] itemDrops;
	
	// TODO add knockback
	
	private Vector2 knockbackVelocity = new Vector2(); // knockback is applied once, at the start, as a velocity. The mob is moved with this velocity constantly, slowing down at a fixed rate, until the knockback is gone.
	
	public Mob(@NotNull String spriteName, int health, @NotNull ItemDrop... deathDrops) {
		super(new Sprite());
		dir = Direction.DOWN;
		this.health = health;
		this.itemDrops = deathDrops;
		
		animator = new MobAnimationController(this, spriteName);
	}
	
	public Direction getDirection() { return dir; }
	
	public void render(SpriteBatch batch, float delta) {
		setSprite(animator.pollAnimation(delta));
		super.render(batch, delta);
	}
	
	@Override
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.setHeight(bounds.getHeight()*4/5);
		return bounds;
	}
	
	@Override
	public void move(float xd, float yd) {
		super.move(xd, yd);
		
		if(xd != 0 || yd != 0) animator.requestState(AnimationState.WALK);
		
		Direction dir = Direction.getDirection(xd, yd);
		if(dir != null) {
			// change sprite direction
			this.dir = dir;
		}
	}
	
	@Override
	public boolean hurtBy(WorldObject obj, int damage) {
		health -= Math.min(damage, health);
		if(health == 0) {
			Level level = getLevel();
			if(level != null) {
				for (ItemDrop drop : itemDrops)
					drop.dropItems(level, this, obj);
				level.removeEntity(this);
			}
		}
		
		return true;
	}
	
	
	public boolean maySpawn(Tile tile) {
		TileType type = tile.getType();
		return type == TileType.GRASS || type == TileType.DIRT || type == TileType.SAND;
	}
}
