package miniventure.game.world.entity;

import miniventure.game.GameCore;
import miniventure.game.world.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public abstract class Entity {
	
	private Sprite sprite;
	
	public Entity(Sprite sprite) { this.sprite = sprite; }
	
	public abstract void update(float delta);
	
	public void render(SpriteBatch batch, float delta) { sprite.draw(batch); }
	
	protected void setSprite(TextureRegion texture) {
		float x = sprite.getX(), y = sprite.getY();
		sprite = new Sprite(texture);
		sprite.setPosition(x, y);
	}
	
	public Rectangle getBounds() { return sprite.getBoundingRectangle(); }
	
	public void move(int xd, int yd) {
		moveAxis(true, xd);
		moveAxis(false, yd);
	}
	
	private void moveAxis(boolean xaxis, int amt) {
		Rectangle oldRect = sprite.getBoundingRectangle();
		Rectangle newRect = new Rectangle(sprite.getX()+(xaxis?amt:0), sprite.getY()+(xaxis?0:amt), sprite.getWidth(), sprite.getHeight());
		
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
		newTiles.removeAll(level.getOverlappingTiles(oldRect), true); // "true" means use == for comparison rather than .equals()
		
		// we now have a list of the tiles that will be touched, but aren't now.
		boolean canMove = true;
		for(Tile tile: newTiles) {
			tile.touchedBy(this); // NOTE: I can see this causing an issue if you move too fast; it will "touch" tiles that could be far away, if the player will move there next frame.
			canMove = canMove && tile.isPermeableBy(this);
		}
		
		if(!canMove) return; // don't bother interacting with entities if tiles prevent movement.
		
		// get and touch entities, and check for blockage
		
		Array<Entity> newEntities = level.getOverlappingEntities(newRect);
		newEntities.removeAll(level.getOverlappingEntities(oldRect), true); // because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(Entity entity: newEntities) {
			if(!entity.touchedBy(this))
				this.touchedBy(entity); // to make sure something has a chance to happen, but it doesn't happen twice.
			
			canMove = canMove && !blockedBy(entity);
		}
		
		if(!canMove) return;
		
		// FINALLY, the entity can move.
		moveTo((int)newRect.x, (int)newRect.y);
	}
	
	public void moveTo(int x, int y) {
		// this method doesn't care where you end up.
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		x = Math.min(x, GameCore.SCREEN_WIDTH);
		y = Math.min(y, GameCore.SCREEN_HEIGHT);
		sprite.setPosition(x, y);
	}
	
	// returns whether anything meaningful happened; if false, then other.touchedBy(this) will be called.
	public boolean touchedBy(Entity other) { return false; }
	
	// returns whether the other entity stops this one from moving.
	// Generally, entities are tangible and stop each other from moving, so this returns true by default.
	public boolean blockedBy(Entity other) { return true; }
}
