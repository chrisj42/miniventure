package miniventure.game.world.entity.particle;

import miniventure.game.util.FrameBlinker;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class BounceEntity extends Entity {
	
	private static final float GRAVITY = -50;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	
	private final float lifetime;
	
	private Vector3 velocity; // in tiles / second.
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	private FrameBlinker blinker;
	
	private float lastBounceTime; // used to halt the entity once it starts bouncing a lot really quickly.
	
	
	public BounceEntity(TextureRegion texture, float lifetime) {
		this(texture, new Vector2().setToRandomDirection(), lifetime);
	}
	public BounceEntity(TextureRegion texture, Vector2 goalDir, float lifetime) {
		super(texture);
		this.lifetime = lifetime;
		
		blinker = new FrameBlinker(1, 1, false);
		
		velocity = new Vector3(goalDir.cpy().nor().scl(MathUtils.random(0.5f, 2.5f)), MathUtils.random(8f, 12f));
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
		Vector3 vel = velocity.cpy().scl(delta);
		move(vel.x, vel.y, vel.z);
		Vector2 newPos = getPosition();
		
		if(newPos.x != pos.x+vel.x)
			velocity.x *= -1;
		if(newPos.y != pos.y+vel.y)
			velocity.y *= -1;
		
		time += delta;
		
		if(getZ() < 0) {
			setZ(0);
			velocity.scl(REBOUND_SPEED_FACTOR, REBOUND_SPEED_FACTOR, -REBOUND_SPEED_FACTOR);
			if(time - lastBounceTime < 0.01f) {
				moving = false;
				velocity.setZero();
			}
			else
				lastBounceTime = time;
		}
		
		if(moving)
			velocity.add(0, 0, GRAVITY*delta);
		
		if(time > lifetime)
			remove();
	}
	
	@Override
	public boolean isPermeableBy(Entity entity, boolean delegate) { return true; }
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		blinker.update(delta);
		
		if(time < lifetime * BLINK_THRESHOLD || blinker.shouldRender())
			super.render(batch, delta, posOffset);
	}
}
