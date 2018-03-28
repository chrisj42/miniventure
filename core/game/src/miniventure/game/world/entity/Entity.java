package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.util.Blinker;
import miniventure.game.world.Boundable;
import miniventure.game.world.Level;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Entity implements WorldObject {
	
	@NotNull private final WorldManager world;
	
	private final int eid;
	float x, y, z = 0;
	
	@NotNull private EntityRenderer renderer = EntityRenderer.BLANK;
	private BlinkRenderer blinker = null;
	
	// for server
	public Entity(@NotNull WorldManager world) {
		this.world = world;
		
		eid = world.registerEntityWithNewId(this);
	}
	
	// for client
	public Entity(@NotNull WorldManager world, int eid) {
		this.world = world;
		this.eid = eid;
		world.registerEntity(this);
	}
	
	public int getId() { return eid; }
	
	@NotNull @Override
	public WorldManager getWorld() { return world; }
	
	@Override @Nullable
	public Level getLevel() { return world.getEntityLevel(this); }
	
	/// this is called only to remove an entity completely from the game, not to change levels.
	public void remove() {
		world.deregisterEntity(eid);
	}
	
	@Override
	public void update(float delta) {}
	
	public void setRenderer(@NotNull EntityRenderer renderer) {
		this.renderer = renderer;
		if(blinker != null)
			blinker.setRenderer(renderer);
	}
	public void setBlinker(float initialDuration, boolean blinkFirst, Blinker blinker) {
		this.blinker = new BlinkRenderer(renderer, initialDuration, blinkFirst, blinker);
	}
	@NotNull public EntityRenderer getRenderer() {
		if(blinker != null) return blinker;
		return renderer;
	}
	@NotNull public EntityRenderer getMainRenderer() { return renderer; }
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		renderer.update(delta);
		if(blinker != null) blinker.update(delta);
		
		//System.out.println("rendering entity "+this+" at "+getPosition(true));
		getRenderer().render((x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE, batch);
	}
	
	protected Rectangle getUnscaledBounds() {
		Vector2 size = renderer.getSize();
		return new Rectangle(x, y, size.x, size.y);
	}
	
	public Vector3 getLocation() { return new Vector3(x, y, z); }
	public Vector3 getLocation(boolean worldOriginCenter) { return new Vector3(getPosition(worldOriginCenter), z); }
		
	@Override @NotNull
	public Rectangle getBounds() {
		Rectangle bounds = getUnscaledBounds();
		bounds.width /= Tile.SIZE;
		bounds.height /= Tile.SIZE;
		return bounds;
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item item) { return false; }
	
	public boolean move(Vector2 v) { return move(v.x, v.y); }
	public boolean move(Vector3 v) { return move(v.x, v.y, v.z); }
	public boolean move(float xd, float yd) { return move(xd, yd, 0); }
	public boolean move(float xd, float yd, float zd) {
		Level level = getLevel();
		if(level == null) return false; // can't move if you're not in a level...
		Vector2 movement = new Vector2();
		movement.x = moveAxis(level, true, xd, 0);
		movement.y = moveAxis(level, false, yd, movement.x);
		z += zd;
		boolean moved = !movement.isZero();
		if(moved)
			moveTo(level, x+movement.x, y+movement.y);
		return moved;
	}
	
	protected float moveAxis(@NotNull Level level, boolean xaxis, float amt, float other) {
		if(amt == 0) return 0;
		Rectangle oldRect = getBounds();
		oldRect.setPosition(x+(xaxis?0:other), y+(xaxis?other:0));
		Rectangle newRect = new Rectangle(oldRect.x+(xaxis?amt:0), oldRect.y+(xaxis?0:amt), oldRect.width, oldRect.height);
		
		// check and see if the entity can go to the new coordinates.
		/*
			We can do this by:
				- finding entities in the new occupied area that wasn't in the old area, and seeing if any of them prevent this entity from moving
				- determining which tiles the entity is going to touch, that it isn't already in, and checking to see if any of them prevent movement
				- calling any interaction methods along the way
		 */
		
		Array<Tile> futureTiles = level.getOverlappingTiles(newRect);
		Array<Tile> currentTiles = level.getOverlappingTiles(oldRect);
		Array<Tile> newTiles = new Array<>(futureTiles);
		
		
		newTiles.removeAll(currentTiles, false); // "true" means use == for comparison rather than .equals()
		
		// we now have a list of the tiles that will be touched, but aren't now.
		boolean canMoveCurrent = false;
		for(Tile tile: currentTiles) // if any are permeable, then don't let the player escape to new impermeable tiles.
			canMoveCurrent = canMoveCurrent || tile.isPermeableBy(this);
		
		boolean canMove = true;
		for(Tile tile: newTiles) {
			touchTile(tile);
			canMove = canMove && (!canMoveCurrent || tile.isPermeableBy(this));
		}
		
		if(canMove && canMoveCurrent) {
			Array<Tile> oldTiles = new Array<>(currentTiles);
			oldTiles.removeAll(futureTiles, false);
			
			Array<Tile> sameTiles = new Array<>(futureTiles);
			sameTiles.removeAll(newTiles, false);
			
			// check the sameTiles; if at least one is not permeable, and at least one oldTile is, then stop the move.
			boolean canMoveOld = false, canMoveSame = true;
			for(Tile oldTile: oldTiles)
				canMoveOld = canMoveOld || oldTile.isPermeableBy(this);
			for(Tile sameTile: sameTiles)
				canMoveSame = canMoveSame && sameTile.isPermeableBy(this);
			
			if(!canMoveSame && canMoveOld)
				canMove = false;
		}
		
		if(!canMove) return 0; // don't bother interacting with entities if tiles prevent movement.
		
		// get and touch entities, and check for blockage
		
		Array<Entity> newEntities = level.getOverlappingEntities(newRect);
		newEntities.removeAll(level.getOverlappingEntities(oldRect), true); // because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(Entity entity: newEntities) {
			touchEntity(entity);
			canMove = canMove && entity.isPermeableBy(this);
		}
		
		if(!canMove) return 0;
		
		// the entity can move.
		
		return amt;
	}
	
	void touchTile(Tile tile) {}
	void touchEntity(Entity entity) {}
	
	public void moveTo(@NotNull Level level, @NotNull Vector2 pos) { moveTo(level, pos.x, pos.y); }
	public void moveTo(@NotNull Level level, float x, float y) {
		//if(level == getLevel() && x == this.x && y == this.y) return; // no action or updating required.
		
		// this method doesn't care where you end up.
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		Vector2 size = getSize();
		x = Math.min(x, level.getWidth() - size.x);
		y = Math.min(y, level.getHeight() - size.y);
		
		this.x = x;
		this.y = y;
		
		if(level == getLevel())
			level.entityMoved(this);
		else
			world.setEntityLevel(this, level);
	}
	public void moveTo(@NotNull Level level, float x, float y, float z) {
		moveTo(level, x, y);
		this.z = z;
	}
	public void moveTo(@NotNull Tile tile) {
		Vector2 pos = tile.getCenter();
		pos.sub(getSize().scl(0.5f));
		moveTo(tile.getLevel(), pos);
	}
	
	protected void moveIfLevel(float x, float y) {
		Level level = getLevel();
		if(level != null)
			moveTo(level, x, y);
	}
	
	// returns whether anything meaningful happened; if false, then other.touchedBy(this) will be called.
	@Override
	public boolean touchedBy(Entity other) { return false; }
	
	@Override
	public void touching(Entity entity) {}
	
	@Override
	public final boolean isPermeableBy(Entity e) { return isPermeable(); }
	public boolean isPermeable() { return false; }
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) { return false; }
	
	@Override
	public boolean equals(Object other) { return other instanceof Entity && ((Entity)other).eid == eid; }
	
	@Override
	public int hashCode() { return eid; }
	
	
	public static class EntityTag implements Tag<Entity> {
		public final int eid;
		
		private EntityTag() { this(0); }
		public EntityTag(int eid) {
			this.eid = eid;
		}
		
		@Override
		public Entity getObject(WorldManager world) { return world.getEntity(eid); }
	}
	
	@Override
	public EntityTag getTag() { return new EntityTag(eid); }
	
	@Override
	public String toString() { return Integer.toHexString(super.hashCode())+"_"+getClass().getSimpleName()+"-"+eid; }
	
	public int superHash() { return super.hashCode(); }
}
