package miniventure.game.world.entity;

import miniventure.game.util.FrameBlinker;
import miniventure.game.world.Level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class BounceEntity extends Entity {
	
	private static final float GRAVITY = -4;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	
	private final float lifetime;
	
	private Vector3 velocity; // in tiles / second.
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	private FrameBlinker blinker;
	
	
	public BounceEntity(Sprite sprite, float lifetime) {
		this(sprite, new Vector2().setToRandomDirection(), lifetime);
	}
	public BounceEntity(Sprite sprite, Vector2 goalDir, float lifetime) {
		super(sprite);
		this.lifetime = lifetime;
		
		blinker = new FrameBlinker(1, 1, false);
		
		velocity = new Vector3(goalDir.cpy().nor().scl(MathUtils.random(0.8f, 1.5f)), MathUtils.random(0.8f, 2f));
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
		
		Vector2 pos = getPosition();
		Vector3 velocity = this.velocity.cpy().scl(delta);
		move(velocity.x, velocity.y, velocity.z);
		Vector2 newPos = getPosition();
		
		if(newPos.x != pos.x+velocity.x)
			this.velocity.x *= -1;
		if(newPos.y != pos.y+velocity.y)
			this.velocity.y *= -1;
		
		if(getZ() < 0) {
			setZ(0);
			this.velocity.scl(REBOUND_SPEED_FACTOR, REBOUND_SPEED_FACTOR, -REBOUND_SPEED_FACTOR);
			if(velocity.len() < 0.001f) {
				moving = false;
				velocity.setZero();
			}
		}
		
		if(moving)
			velocity.add(0, 0, GRAVITY*delta);
		
		time += delta;
		
		if(time > lifetime)
			remove();
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		blinker.update(delta);
		
		if(time < lifetime * BLINK_THRESHOLD || blinker.shouldRender())
			super.render(batch, delta, posOffset);
	}
}
