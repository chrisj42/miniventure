package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.network.GameProtocol.PositionUpdate;
import miniventure.game.util.blinker.Blinker;
import miniventure.game.util.pool.RectPool;
import miniventure.game.util.pool.Vector3Pool;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Entity implements WorldObject {
	
	@NotNull private final WorldManager world;
	
	private final EntityTag tag;
	private final int eid;
	float x, y, z = 0;
	
	@NotNull private EntityRenderer renderer = EntityRenderer.BLANK;
	private BlinkRenderer blinker = null;
	
	// for entities updated locally (called by both server and client for different entities)
	// "local" refers to if the entity exists only locally, as opposed to being synced across clients.
	protected Entity(@NotNull WorldManager world, boolean negative) {
		this.world = world;
		eid = world.reserveNewEntityId(negative);
		this.tag = new EntityTag(eid);
	}
	
	// for client, on entities updated by the server
	protected Entity(@NotNull WorldManager world, int eid, PositionUpdate position) {
		this.world = world;
		this.eid = eid;
		x = position.x;
		z = position.z;
		y = position.y;
		this.tag = new EntityTag(eid);
		// world.registerEntity(this); // considered to be part of the level as well
	}
	
	public int getId() { return eid; }
	
	@NotNull @Override
	public WorldManager getWorld() { return world; }
	
	@Override @Nullable
	public Level getLevel() { return world.getEntityLevel(this); }
	
	public abstract boolean isMob();
	public boolean isFloating() { return false; }
	
	/// this is called only to remove an entity completely from the game, not to change levels.
	public void remove() { world.deregisterEntity(eid); }
	
	public void update(float delta) {}
	
	public void setRenderer(@NotNull EntityRenderer renderer) {
		this.renderer = renderer;
		if(blinker != null)
			blinker.setRenderer(renderer);
	}
	public void setBlinker(@Nullable Color blinkColor, float initialDuration, boolean blinkFirst, Blinker blinker) {
		this.blinker = new BlinkRenderer(renderer, blinkColor, initialDuration, blinkFirst, blinker);
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
	}
	
	protected Rectangle getUnscaledBounds() {
		Vector2 size = renderer.getSize();
		Rectangle rect = RectPool.POOL.obtain(x, y, size.x, size.y);
		VectorPool.POOL.free(size);
		return rect;
	}
	
	public Vector3 getLocation() { return Vector3Pool.POOL.obtain(x, y, z); }
	
	public float getZ() { return z; }
	public void setZ(float z) { this.z = z; }
	
	@Override @NotNull
	public Rectangle getBounds() {
		Rectangle bounds = getUnscaledBounds();
		bounds.width /= Tile.SIZE;
		bounds.height /= Tile.SIZE;
		return bounds;
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item item) { return Result.NONE; }
	
	public boolean move(Vector2 v) { return move(v, false); }
	public boolean move(Vector2 v, boolean free) {
		final boolean res = move(v.x, v.y);
		if(free) VectorPool.POOL.free(v);
		return res;
	}
	public boolean move(Vector3 v) { return move(v, false); }
	public boolean move(Vector3 v, boolean free) {
		final boolean res = move(v.x, v.y, v.z);
		if(free) Vector3Pool.POOL.free(v);
		return res;
	}
	public boolean move(float xd, float yd) { return move(xd, yd, 0); }
	public boolean move(float xd, float yd, float zd) {
		Level level = getLevel();
		if(level == null) return false; // can't move if you're not in a level...
		Vector2 movement = VectorPool.POOL.obtain();
		movement.x = moveAxis(level, true, xd, 0);
		movement.y = moveAxis(level, false, yd, movement.x);
		z += zd;
		boolean moved = !movement.isZero();
		if(moved)
			moveTo(x+movement.x, y+movement.y);
		VectorPool.POOL.free(movement);
		return moved;
	}
	
	/// moves the entity along an axis, given there are no collisions. calls touch methods during move.
	private float moveAxis(@NotNull Level level, boolean xaxis, float amt, float other) {
		if(amt == 0) return 0;
		Rectangle oldRect = getBounds();
		oldRect.setPosition(x+(xaxis?0:other), y+(xaxis?other:0));
		Rectangle newRect = RectPool.POOL.obtain(oldRect.x+(xaxis?amt:0), oldRect.y+(xaxis?0:amt), oldRect.width, oldRect.height);
		
		final float moveAmt;
		if(checkMove(level, oldRect, newRect, true))
			moveAmt = amt;
		else
			moveAmt = 0;
		
		RectPool.POOL.free(oldRect);
		RectPool.POOL.free(newRect);
		
		return moveAmt;
	}
	/// public method to check if an entity can be moved in a given fashion. Does not call touch methods.
	public static boolean checkMove(@NotNull Entity entity, @NotNull Level level, Rectangle oldRect, Rectangle newRect) {
		return entity.checkMove(level, oldRect, newRect, false);
	}
	
	private boolean checkMove(@NotNull Level level, Rectangle oldRect, Rectangle newRect, boolean interact) {
		
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
			canMoveCurrent = canMoveCurrent || canPermeate(tile);
		
		boolean canMove = true;
		for(Tile tile: newTiles) {
			if(interact) {
				tile.touchedBy(this, true);
				this.touchedTile(tile, true);
			}
			canMove = canMove && (!canMoveCurrent || canPermeate(tile));
		}
		
		if(canMove && canMoveCurrent) {
			Array<Tile> oldTiles = new Array<>(currentTiles);
			oldTiles.removeAll(futureTiles, false);
			
			Array<Tile> sameTiles = new Array<>(futureTiles);
			sameTiles.removeAll(newTiles, false);
			
			// check the sameTiles; if at least one is not permeable, and at least one oldTile is, then stop the move.
			boolean canMoveOld = false, canMoveSame = true;
			for(Tile oldTile: oldTiles)
				canMoveOld = canMoveOld || canPermeate(oldTile);
			for(Tile sameTile: sameTiles)
				canMoveSame = canMoveSame && canPermeate(sameTile);
			
			if(!canMoveSame && canMoveOld)
				canMove = false;
		}
		
		if(!canMove) return false; // don't bother interacting with entities if tiles prevent movement.
		
		// get and touch entities, and check for blockage
		
		Array<Entity> newEntities = new Array<>(Entity.class);
		level.forOverlappingEntities(newRect, newEntities::add);
		level.forOverlappingEntities(oldRect, e -> newEntities.removeValue(e, true));
		// because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(Entity entity: newEntities) {
			if(interact) {
				// do an initial touch interaction both ways
				entity.touchedBy(this, true);
				this.touchedBy(entity, true);
			}
			canMove = canMove && entity.isPermeable();
		}
		
		if(!canMove) return false;
		
		// the entity can move.
		
		return true;
	}
	
	protected void touchedTile(Tile tile, boolean initial) {}
	
	public void moveTo(@NotNull Vector2 pos) { moveTo(pos, false); }
	public void moveTo(@NotNull Vector2 pos, boolean free) {
		moveTo(pos.x, pos.y);
		if(free) VectorPool.POOL.free(pos);
	}
	public void moveTo(@NotNull Vector3 pos) { moveTo(pos, false); }
	public void moveTo(@NotNull Vector3 pos, boolean free) {
		moveTo(pos.x, pos.y, pos.z);
		if(free) Vector3Pool.POOL.free(pos);
	}
	public void moveTo(float x, float y) { moveTo(x, y, this.z); }
	public void moveTo(float x, float y, float z) {
		// this method doesn't care where you end up; ie doesn't check for collisions.
		// it also doesn't bother with clamping if there is no level.
		
		Level level = getLevel();
		if(level != null) {
			x = Math.max(x, 0);
			y = Math.max(y, 0);
			Vector2 size = getSize();
			x = Math.min(x, level.getWidth() - size.x);
			y = Math.min(y, level.getHeight() - size.y);
			VectorPool.POOL.free(size);
		}
		
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void moveTo(@NotNull Tile tile) {
		Vector2 pos = tile.getCenter();
		Vector2 size = getSize();
		pos.sub(size.scl(0.5f));
		moveTo(pos, true);
		VectorPool.POOL.free(size);
	}
	
	protected void moveIfLevel(float x, float y) {
		Level level = getLevel();
		if(level != null)
			moveTo(x, y);
	}
	
	@Override
	public void touchedBy(Entity other, boolean initial) {}
	
	@Override
	public boolean isPermeable() { return false; }
	
	public boolean canPermeate(Tile tile) { return isFloating() || tile.isPermeable(); }
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item attackItem, int damage) { return Result.NONE; }
	
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
	public EntityTag getTag() { return tag; }
	
	@Override
	public String toString() { return Integer.toHexString(super.hashCode())+'_'+getClass().getSimpleName()+'-'+eid; }
	
	public int superHash() { return super.hashCode(); }
}
