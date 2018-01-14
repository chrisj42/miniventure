package miniventure.game.world.entity;

import miniventure.game.world.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class BounceEntity extends Entity {
	
	private static final float INITIAL_BOUNCE_FORCE = 5, INITIAL_MOVE_FORCE = 0.04f;
	private static final float GRAVITY = -0.5f;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	private static final float BLINK_RATE = 0.005f; // the rate at which the entity blinks, in seconds/blink.
	
	private final float lifetime;
	
	private Vector3 velocity;
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	
	public BounceEntity(Sprite sprite, float lifetime) {
		this(sprite, new Vector2().setToRandomDirection().scl(Tile.SIZE * MathUtils.random(0.8f, 1.5f)), lifetime);
	}
	public BounceEntity(Sprite sprite, Vector2 goalDir, float lifetime) {
		super(sprite);
		this.lifetime = lifetime;
		
		velocity = new Vector3(goalDir.cpy().scl(INITIAL_MOVE_FORCE), INITIAL_BOUNCE_FORCE);
	}
	
	float getTime() { return time; }
	
	private boolean moving = true;
	@Override
	public void update(float delta) {
		//super.update(delta);
		/*
			Movement will work like this:
				- the itemEntity will move along a base axis, as time progresses, and the actual position will vary according to time.
				- b/c we don't know the current position, we will just have to assume the starting point is (0,0), and move the delta dist for this frame, based on the passed in delta and the total move time.
		 */
		
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		Vector2 pos = getBounds().getPosition(new Vector2());
		move(velocity.x, velocity.y, velocity.z);
		Vector2 newPos = getBounds().getPosition(new Vector2());
		
		if(newPos.x != pos.x+velocity.x)
			velocity.x *= -1;
		if(newPos.y != pos.y+velocity.y)
			velocity.y *= -1;
		
		if(getZ() < 0) {
			setZ(0);
			velocity.x *= REBOUND_SPEED_FACTOR;
			velocity.y *= REBOUND_SPEED_FACTOR;
			velocity.z *= -REBOUND_SPEED_FACTOR;
			if(velocity.len() < 0.001f) {
				moving = false;
				velocity.setZero();
			}
		}
		
		if(moving)
			velocity.add(0, 0, GRAVITY);
		
		time += delta;
		
		if(time > lifetime)
			remove();
	}
	
	protected boolean shouldRender() {
		return !(
			time >= lifetime * BLINK_THRESHOLD &&
			time % (BLINK_RATE * 2) > BLINK_RATE
		);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		if(shouldRender())
			super.render(batch, delta);
	}
}
