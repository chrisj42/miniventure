package miniventure.game.world.entity;

import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.util.blinker.Blinker;
import miniventure.game.world.Level;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.particle.ParticleData;
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
	
	private final int eid;
	float x, y, z = 0;
	
	@NotNull private EntityRenderer renderer = EntityRenderer.BLANK;
	private BlinkRenderer blinker = null;
	
	// for entities updated locally (called by both server and client for different entities)
	public Entity(@NotNull WorldManager world) {
		this.world = world;
		
		eid = world.registerEntityWithNewId(this, this instanceof ParticleData);
	}
	
	// for client, on entities updated by the server
	public Entity(@NotNull WorldManager world, int eid, PositionUpdate position) {
		this.world = world;
		this.eid = eid;
		x = position.x;
		y = position.y;
		z = position.z;
		world.registerEntity(this); // considered to be part of the level as well
	}
	
	public int getId() { return eid; }
	
	@NotNull @Override
	public WorldManager getWorld() { return world; }
	
	@Override @Nullable
	public Level getLevel() { return world.getEntityLevel(this); }
	
	public abstract boolean isMob();
	public boolean isFloating() { return false; }
	
	/// this is called only to remove an entity completely from the game, not to change levels.
	public void remove() {
		world.deregisterEntity(eid);
	}
	
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
		return new Rectangle(x, y, size.x, size.y);
	}
	
	public Vector3 getLocation() { return new Vector3(x, y, z); }
	public Vector3 getLocation(boolean worldOriginCenter) { return new Vector3(getPosition(worldOriginCenter), z); }
	
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
			moveTo(x+movement.x, y+movement.y);
		return moved;
	}
	
	/// moves the entity along an axis, given there are no collisions. calls touch methods during move.
	private float moveAxis(@NotNull Level level, boolean xaxis, float amt, float other) {
		if(amt == 0) return 0;
		Rectangle oldRect = getBounds();
		oldRect.setPosition(x+(xaxis?0:other), y+(xaxis?other:0));
		Rectangle newRect = new Rectangle(oldRect.x+(xaxis?amt:0), oldRect.y+(xaxis?0:amt), oldRect.width, oldRect.height);
		
		if(checkMove(level, oldRect, newRect, true))
			return amt;
		else
			return 0;
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
			if(interact)
				touchTile(tile);
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
		
		Array<Entity> newEntities = level.getOverlappingEntities(newRect);
		newEntities.removeAll(level.getOverlappingEntities(oldRect), true); // because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(Entity entity: newEntities) {
			if(interact)
				touchEntity(entity);
			canMove = canMove && entity.isPermeable();
		}
		
		if(!canMove) return false;
		
		// the entity can move.
		
		return true;
	}
	
	abstract void touchTile(Tile tile);
	abstract void touchEntity(Entity entity);
	
	public void moveTo(@NotNull Vector2 pos) { moveTo(pos.x, pos.y); }
	public void moveTo(@NotNull Vector3 pos) { moveTo(pos.x, pos.y, pos.z); }
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
		}
		
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void moveTo(@NotNull Tile tile) {
		Vector2 pos = tile.getCenter();
		pos.sub(getSize().scl(0.5f));
		moveTo(pos);
	}
	
	protected void moveIfLevel(float x, float y) {
		Level level = getLevel();
		if(level != null)
			moveTo(x, y);
	}
	
	// returns whether anything meaningful happened; if false, then other.touchedBy(this) will be called.
	@Override
	public boolean touchedBy(Entity other) { return false; }
	
	@Override
	public void touching(Entity entity) {}
	
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
	public EntityTag getTag() { return new EntityTag(eid); }
	
	@Override
	public String toString() { return Integer.toHexString(super.hashCode())+'_'+getClass().getSimpleName()+'-'+eid; }
	
	public int superHash() { return super.hashCode(); }
}
