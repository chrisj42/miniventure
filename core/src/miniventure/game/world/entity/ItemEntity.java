package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.world.Level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ItemEntity extends Entity {
	
	private static final float LIFETIME = 8f;
	
	private static final float INITIAL_BOUNCE_FORCE = 5, INITIAL_MOVE_FORCE = 0.04f;
	private static final float GRAVITY = -0.5f;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private Vector3 velocity;
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	public ItemEntity(Item item, Vector2 goalDir) {
		super(new Sprite(item.getTexture()));
		//position = new Vector3();
		velocity = new Vector3(goalDir.cpy().scl(INITIAL_MOVE_FORCE), INITIAL_BOUNCE_FORCE);
		//System.out.println("new vel: " + velocity);
	}
	
	/*@Override
	public void addedToLevel(Level level) {
		//Vector2 pos = getBounds().getPosition(new Vector2());
		//position.set(pos, 0);
		time = 0;
	}*/
	
	private boolean moving = true;
	@Override
	public void update(float delta) {
		/*
			Movement will work like this:
				- the itemEntity will move along a base axis, as time progresses, and the actual position will vary according to time.
				- b/c we don't know the current position, we will just have to assume the starting point is (0,0), and move the delta dist for this frame, based on the passed in delta and the total move time.
		 */
		
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		if(time > LIFETIME) {
			//System.out.println("time = " + time);
			// remove item entity
			level.removeEntity(this);
		}
		
		Vector2 pos = getBounds().getPosition(new Vector2());
		move(velocity.x, velocity.y, velocity.z);
		Vector2 newPos = getBounds().getPosition(new Vector2());
		
		if(newPos.x != pos.x+velocity.x)
			velocity.x *= -1;
		if(newPos.y != pos.y+velocity.y)
			velocity.y *= -1;
		
		if(getZ() < 0) {
			//System.out.println("bouncing");
			setZ(0);
			velocity.x *= REBOUND_SPEED_FACTOR;
			velocity.y *= REBOUND_SPEED_FACTOR;
			velocity.z *= -REBOUND_SPEED_FACTOR;
			if(velocity.len() < 0.001f) {
				moving = false;
				//System.out.println("set velocity to zero");
				velocity.setZero();
			}
		}
		
		if(moving) {
			velocity.add(0, 0, GRAVITY);
			//System.out.println("velocity = " + velocity);
		}
		
		time += delta;
	}
	
	@Override
	public boolean blockedBy(Entity other) { return false; }
}
