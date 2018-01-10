package miniventure.game.world.entity;

import java.util.HashMap;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class Entity implements WorldObject {
	
	private static final HashMap<Integer, Entity> takenIDs = new HashMap<>();
	
	private Sprite sprite;
	private final int eid;
	private float z = 0;
	
	public Entity(Sprite sprite) {
		this.sprite = sprite;
		
		int eid;
		do {
			eid = MathUtils.random.nextInt();
		} while(takenIDs.containsKey(eid));
		this.eid = eid;
		takenIDs.put(eid, this);
	}
	
	@Override
	public Level getLevel() { return Level.getEntityLevel(this); }
	
	@Override
	public abstract void update(float delta);
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		float prevY = sprite.getY();
		sprite.setY(prevY+z);
		sprite.draw(batch);
		sprite.setY(prevY);
	}
	
	protected void setSprite(TextureRegion texture) {
		//sprite.setRegion(texture);
		float x = sprite.getX(), y = sprite.getY();
		sprite = new Sprite(texture);
		sprite.setPosition(x, y);
		z = 0;
	}
	
	float getZ() { return z; }
	void setZ(float z) { this.z = z; }
	
	@Override
	public Rectangle getBounds() {
		//bounds.setX(bounds.getX()+bounds.getWidth()/5);
		//bounds.setSize(bounds.getWidth()*3/5, bounds.getHeight()/2); // due to the weird perspective of the game, the top part of most sprites is technically "in the air", so you don't really touch it.
		//bounds.setHeight(bounds.getHeight()*4/5);
		return new Rectangle(sprite.getBoundingRectangle());
	}
	
	public void addedToLevel(Level level) {}
	
	public boolean interactWith(Player player, Item item) { return false; }
	
	public void move(float xd, float yd) {
		move(xd, yd, 0);
	}
	public void move(float xd, float yd, float zd) {
		moveAxis(true, xd);
		moveAxis(false, yd);
		z += zd;
	}
	
	private void moveAxis(boolean xaxis, float amt) {
		if(amt == 0) return;
		Rectangle oldRect = sprite.getBoundingRectangle();
		Rectangle newRect = new Rectangle(sprite.getX()+(xaxis?amt:0), sprite.getY()+(xaxis?0:amt), oldRect.width, oldRect.height);
		
		// check and see if the entity can go to the new coordinates.
		/*
			We can do this by:
				- finding entities in the new occupied area that wasn't in the old area, and seeing if any of them prevent this entity from moving
				- determining which tiles the entity is going to touch, that it isn't already in, and checking to see if any of them prevent movement
				- calling any interaction methods along the way
		 */
		
		Level level = Level.getEntityLevel(this);
		if(level == null) return; // can't move if you're not in a level...
		
		Array<Tile> newTiles = level.getOverlappingTiles(newRect);
		Array<Tile> oldTiles = level.getOverlappingTiles(oldRect);
		newTiles.removeAll(oldTiles, true); // "true" means use == for comparison rather than .equals()
		
		// we now have a list of the tiles that will be touched, but aren't now.
		boolean canMoveCurrent = true;
		for(Tile tile: oldTiles) {
			canMoveCurrent = canMoveCurrent && tile.isPermeableBy(this);
		}
		boolean canMove = true;
		for(Tile tile: newTiles) {
			tile.touchedBy(this); // NOTE: I can see this causing an issue if you move too fast; it will "touch" tiles that could be far away, if the player will move there next frame.
			canMove = canMove && (!canMoveCurrent || tile.isPermeableBy(this));
		}
		
		if(!canMove) return; // don't bother interacting with entities if tiles prevent movement.
		
		// get and touch entities, and check for blockage
		
		Array<Entity> newEntities = level.getOverlappingEntities(newRect);
		newEntities.removeAll(level.getOverlappingEntities(oldRect), true); // because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(Entity entity: newEntities) {
			if(!entity.touchedBy(this))
				this.touchedBy(entity); // to make sure something has a chance to happen, but it doesn't happen twice.
			
			canMove = canMove && entity.isPermeableBy(this);
		}
		
		if(!canMove) return;
		
		// FINALLY, the entity can move.
		moveTo(level, newRect.x, newRect.y);
	}
	
	public void moveTo(Level level, Vector2 pos) { moveTo(level, pos.x, pos.y); }
	public void moveTo(Level level, float x, float y) {
		// this method doesn't care where you end up.
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		x = Math.min(x, level.getWidth()*Tile.SIZE - sprite.getRegionWidth());
		y = Math.min(y, level.getHeight()*Tile.SIZE - sprite.getRegionHeight());
		sprite.setPosition(x, y);
	}
	public void moveTo(Tile tile) {
		int x = tile.getCenterX() - sprite.getRegionWidth()/2;
		int y = tile.getCenterY() - sprite.getRegionHeight()/2;
		moveTo(tile.getLevel(), x, y);
	}
	
	// returns whether anything meaningful happened; if false, then other.touchedBy(this) will be called.
	@Override
	public boolean touchedBy(Entity other) { return false; }
	
	// returns whether the other entity stops this one from moving.
	// Generally, entities are tangible and stop each other from moving, so this returns true by default.
	//public boolean blockedBy(Entity other) { return !(other instanceof ItemEntity); }
	
	@Override
	public boolean isPermeableBy(Entity entity) { return entity instanceof ItemEntity; }
	
	@Override
	public boolean attackedBy(Mob mob, Item attackItem) { return hurtBy(mob, attackItem.getDamage(this)); }
	
	@Override
	public boolean hurtBy(WorldObject obj, int dmg) { return false; } // generally speaking, attacking an entity doesn't do anything; only for mobs, and maybe furniture...
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Entity && ((Entity)other).eid == eid;
	}
	
	@Override
	public int hashCode() {
		return eid;
	}
}
