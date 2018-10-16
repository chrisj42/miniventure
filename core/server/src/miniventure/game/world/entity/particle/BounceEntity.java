package miniventure.game.world.entity.particle;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.world.entity.ClassDataList;
import miniventure.game.util.Version;
import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.Level;
import miniventure.game.world.entity.ServerEntity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.Nullable;

public abstract class BounceEntity extends ServerEntity {
	
	private static final float GRAVITY = -50;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	
	private final float lifetime;
	
	private Vector3 velocity; // in tiles / second.
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	private float lastBounceTime; // used to halt the entity once it starts bouncing a lot really quickly.
	
	public BounceEntity(@Nullable Vector2 goalDir, float lifetime) {
		super();
		this.lifetime = lifetime;
		if(goalDir == null)
			goalDir = new Vector2().setToRandomDirection();
		
		velocity = new Vector3(goalDir, MathUtils.random(8f, 12f));
		
		setBlinker(lifetime * BLINK_THRESHOLD, false, new FrameBlinker(1, 1, false));
	}
	
	protected BounceEntity(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(1);
		lifetime = Float.parseFloat(data.get(0));
		float x = Float.parseFloat(data.get(1));
		float y = Float.parseFloat(data.get(2));
		float z = Float.parseFloat(data.get(3));
		velocity = new Vector3(x, y, z);
		time = Float.parseFloat(data.get(4));
		lastBounceTime = Float.parseFloat(data.get(5));
		
		setBlinker(lifetime * BLINK_THRESHOLD, false, new FrameBlinker(1, 1, false));
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			String.valueOf(lifetime),
			String.valueOf(velocity.x),
			String.valueOf(velocity.y),
			String.valueOf(velocity.z),
			String.valueOf(time),
			String.valueOf(lastBounceTime)
		));
		
		allData.add(data);
		return allData;
	}
	
	protected void scaleVelocity(float amt) { velocity.scl(amt, amt, 1); }
	
	float getTime() { return time; }
	
	private boolean moving = true;
	@Override
	public void update(float delta) {
		super.update(delta);
		/*
			Movement will work like this:
				- the itemEntity will move along a base axis, as time progresses, and the actual position will vary according to time.
				- b/c we don't know the current position, we will just have to assume the starting point is (0,0), and move the delta dist for this frame, based on the passed in delta and the total move time.
		 */
		
		Level level = getWorld().getEntityLevel(this);
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
			move(0, 0, -getZ());
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
	public boolean isPermeable() { return true; }
	
	/*@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		blinker.update(delta);
		
		if(time < lifetime * BLINK_THRESHOLD || blinker.shouldRender())
			super.render(batch, delta, posOffset);
	}*/
}
