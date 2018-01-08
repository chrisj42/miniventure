package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.world.Level;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ItemEntity extends Entity {
	
	//private static final float MOVE_DIST = Tile.SIZE * 1.25f;
	//private static final float MOVE_TIME = 2f, LIFETIME = 8f;
	private static final float LIFETIME = 8f;
	
	private static final float INITIAL_FORCE = 5, MOVE_FORCE = 0.25f;
	private static final float GRAVITY = -0.5f;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	//private Vector2 goalDist; // this represents the path that the origin will travel, throughout a duration MOVE_TIME
	//private Vector2 origin; // origin, that acts as the ground.
	private Vector3 position, velocity;
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	public ItemEntity(Item item, Vector2 goalDir) {
		super(new Sprite(item.getTexture()));
		position = new Vector3();
		velocity = new Vector3(goalDir.cpy().scl(MOVE_FORCE*0), INITIAL_FORCE);
		System.out.println("new vel: " + velocity);
	}
	
	@Override
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.y -= (int)position.z;
		return bounds;
	}
	
	@Override
	public void addedToLevel(Level level) {
		Vector2 pos = getBounds().getPosition(new Vector2());
		position.set(pos, 0);
		time = 0;
	}
	
	//private boolean stop1 = false, stop2 = false;
	//private Vector2 prevVel = new Vector2();
	@Override
	public void update(float delta) {
		/*
			Movement will work like this:
				- the itemEntity will move along a base axis, as time progresses, and the actual position will vary according to time.
				- b/c we don't know the current position, we will just have to assume the starting point is (0,0), and move the delta dist for this frame, based on the passed in delta and the total move time.
		 */
		
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		//if(time > MOVE_TIME) {
			if(time > LIFETIME) {
				System.out.println("time = " + time);
				// remove item entity
				//Level curLevel = Level.getEntityLevel(this);
				//if(curLevel != null)
					level.removeEntity(this);
			}
		//}
		/*if(!stop2) {
			Vector2 move;
			if(time < MOVE_TIME && !stop1) {
				move = goalDist.cpy().scl(delta/MOVE_TIME);
				origin.add(move);
			}
			else move = new Vector2();
			
			Vector2 pos = new Vector2();
			getBounds().getCenter(pos);
			//pos.x += move.x;
			
			if(time == 0) {
				velocity.add(INITIAL_FORCE);
				velocity.x += move.x;
			} else if(pos.y <= origin.y)
				velocity.set(velocity.x, velocity.y*(-REBOUND_SPEED_FACTOR));
			else if(!stop1)
				velocity.add(GRAVITY);
			
			
			pos.add(velocity);
			//if(pos.y <= origin.y)
			//	velocity.y *= -1;
			Vector2 oldPos = new Vector2();
			getBounds().getCenter(oldPos);
			move(pos.x-oldPos.x, pos.y-oldPos.y);
			getBounds().getCenter(oldPos);
			if(oldPos.x != pos.x)
				velocity.x *= -1;
			if(oldPos.y != pos.y)
				velocity.y *= -REBOUND_SPEED_FACTOR;
			
			
			if(velocity.epsilonEquals(velocity.x, prevVel.y, 0.01f) || velocity.epsilonEquals(velocity.x, prevVel.y-move.y, 0.01f) || velocity.epsilonEquals(velocity.x, prevVel.y+move.y, 0.01f)) {
				//System.out.println("y vel="+velocity.y+", zero");
				if(stop1) {
					System.out.println("stayed zero");
					stop2 = true;
					velocity.setZero();
				} else {
					System.out.println("hit zero");
					stop1 = true;
				}
			}
			else {
				System.out.println("y vel="+velocity.y);
				stop1 = false;
			}
			
			prevVel.set(velocity);
		}*/
		
		Vector2 pos = getBounds().getPosition(new Vector2());
		move(velocity.x, velocity.y);
		Vector2 newPos = getBounds().getPosition(new Vector2());
		position.set(newPos.x, newPos.y, position.z);
		position.z += velocity.z;
		moveTo(level, position.x, position.y+(int)position.z);
		
		if(newPos.x != pos.x+velocity.x)
			velocity.x *= -1;
		if(newPos.y != pos.y+velocity.y)
			velocity.y *= -1;
		
		if(position.z < 0 && velocity.z < 0) {
			//position.z = 0; 
			velocity.x *= REBOUND_SPEED_FACTOR;
			velocity.y *= REBOUND_SPEED_FACTOR;
			velocity.z *= -REBOUND_SPEED_FACTOR;
			//velocity.scl(-REBOUND_SPEED_FACTOR);
		}
		
		if(Math.abs(position.z) < 0.05f)
			velocity.add(0, 0, GRAVITY);
		
		/*if(Math.abs(velocity.y) < 0.005f)
			velocity.y = 0;
		if(Math.abs(velocity.x) < 0.005f)
			velocity.x = 0;
		if(Math.abs(velocity.z) < 0.005f)
			velocity.z = 0;*/
		//if(velocity.epsilonEquals(0, 0, 0, 0.005f))
		//	velocity.setZero();
		
		time += delta;
	}
	
	/*private Vector2 getPos() {
		// takes into account time and goalPos.
		
		// y = a(x-h)^2 + k
		/// a = -1
		// find the current parabola.
		float relPos = time / LIFETIME;
		int bounceIdx = 0;
		while(bounceIdx < NUM_BOUNCES && bounceEndLocs[bounceIdx] < relPos)
			bounceIdx++;	
		
		float x, y;
		
		if(bounceIdx >= NUM_BOUNCES) {
			// the item has stopped bouncing.
			// return the position at the end of the last bounce.
			x = bounceEndLocs[bounceEndLocs.length-1];
			y = 0;
		}
		else {
			x = relPos;
			// use x, and current parabola, to find y
			y = (float) (aValues[bounceIdx]*Math.pow(x - xVertices[bounceIdx], 2) + yVertices[bounceIdx]); 
		}
		
		return new Vector2(x, y);
	}*/
	
	/*@Override
	public void render(SpriteBatch batch, float delta) {
		
	}*/
	
	@Override
	public boolean blockedBy(Entity other) { return false; }
}
