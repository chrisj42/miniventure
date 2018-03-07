package miniventure.game.world.entitynew;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Entity implements WorldObject {
	
	@NotNull private final EntityType type;
	@NotNull private final String[] data;
	
	private float x, y, z;
	
	public Entity(@NotNull EntityType type) {
		this.type = type;
		this.data = type.getInitialData();
	}
	public Entity(@NotNull EntityType type, @NotNull String[] data) {
		this.type = type;
		this.data = parseCoords(data);
	}
	
	@NotNull
	// this method assumes the coordinates are in the first three indexes. There then must be at least 3 spots in the array, however no more are required; the returned array will simply be of length 0.
	private String[] parseCoords(@NotNull String[] allData) {
		String[] data = new String[allData.length-3];
		System.arraycopy(allData, 3, data, 0, data.length);
		x = Integer.parseInt(allData[0]);
		y = Integer.parseInt(allData[1]);
		z = Integer.parseInt(allData[2]);
		return data;
	}
	
	@NotNull
	public EntityType getType() { return type; }
	
	@NotNull
	public String[] getData(boolean includeCoords) {
		String[] data = new String[this.data.length+(includeCoords?3:0)];
		System.arraycopy(this.data, 0, data, includeCoords?3:0, this.data.length);
		if(includeCoords) {
			data[0] = x+"";
			data[1] = y+"";
			data[2] = z+"";
		}
		
		return data;
	}
	
	
	@Override @Nullable
	public Level getLevel() { return Level.getEntityLevel(this); }
	
	@Override @Nullable
	public ServerLevel getServerLevel() { return ServerLevel.getEntityLevel(this); }
	
	/// this is called only to remove an entity completely from the game, not to change levels.
	public final void remove() {
		Level level = Level.getEntityLevel(this);
		if(level != null)
			level.removeEntity(this);
	}
	
	@Override
	public Rectangle getBounds() { return null; }
	
	@Override
	public void update(float delta) {
		type.getProp(UpdateProperty.class).update(this, delta);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		
	}
	
	public boolean move(Vector2 v) { return move(v.x, v.y); }
	public boolean move(float xd, float yd) { return move(xd, yd, 0); }
	public boolean move(float xd, float yd, float zd) {
		Level level = getLevel();
		if(level == null) return false; // can't move if you're not in a level...
		Vector2 movement = new Vector2();
		movement.x = moveAxis(level, true, xd, 0);
		movement.y = moveAxis(level, false, yd, movement.x);
		z += zd;
		boolean moved = !movement.isZero();
		if(moved) moveTo(level, x+movement.x, y+movement.y);
		return moved;
	}
	
	private float moveAxis(@NotNull Level level, boolean xaxis, float amt, float other) {
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
			tile.touchedBy(this); // NOTE: I can see this causing an issue if you move too fast; it will "touch" tiles that could be far away, if the player will move there next frame.
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
			if(!entity.touchedBy(this))
				this.touchedBy(entity); // to make sure something has a chance to happen, but it doesn't happen twice.
			
			canMove = canMove && entity.isPermeableBy(this);
		}
		
		if(!canMove) return 0;
		
		// the entity can move.
		
		return amt;
	}
	
	public void moveTo(@NotNull Level level, @NotNull Vector2 pos) { moveTo(level, pos.x, pos.y); }
	public void moveTo(@NotNull Level level, float x, float y) {
		if(level == getLevel() && x == this.x && y == this.y) return; // no action or updating required.
		
		// this method doesn't care where you end up.
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		Vector2 size = getSize();
		x = Math.min(x, level.getWidth() - size.x);
		y = Math.min(y, level.getHeight() - size.y);
		
		// check and see if the entity is changing chunks from their current position.
		// boolean changedChunk = Level.getEntityLevel(this) == level && (
		// 	Chunk.getCoord(x) != Chunk.getCoord(this.x) ||
		// 	Chunk.getCoord(y) != Chunk.getCoord(this.y) );
		
		this.x = x;
		this.y = y;
		
		if(level == getLevel())
			level.entityMoved(this);
		else
			level.addEntity(this);
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
	
	@Override
	public final boolean isPermeableBy(Entity entity) { return isPermeableBy(entity, true); }
	public boolean isPermeableBy(Entity entity, boolean delegate) {
		if(delegate)
			return entity.isPermeableBy(this, false);
		// regular behavior below
		return false;
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) {
		return false;
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int dmg) {
		return false;
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		return false;
	}
	
	@Override
	public void touching(Entity entity) {
		
	}
}
