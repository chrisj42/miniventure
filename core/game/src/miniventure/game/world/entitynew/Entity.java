package miniventure.game.world.entitynew;

import java.util.HashMap;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Entity implements WorldObject {
	
	private final WorldManager world;
	
	private float x, y, z;
	
	private HashMap<EntityPropertyType<? extends EntityProperty>, EntityProperty> singleProperties = new HashMap<>();
	private HashMap<EntityPropertyType<?>, Array<EntityProperty>> propertyLists = new HashMap<>();
	
	public Entity(@NotNull WorldManager world, EntityProperty... properties) {
		this.world = world;
		for(EntityPropertyType<?> type: EntityPropertyType.values) {
			addProps(type, properties);
		}
	}
	
	private <T extends EntityProperty> void addProps(EntityPropertyType<T> type, EntityProperty[] properties) {
		propertyLists.put(type, new Array<>());
		put(type, type.getDefaultInstance(this));
		for(EntityProperty prop: properties) {
			if(type.getPropertyClass().isAssignableFrom(prop.getClass()))
				put(type, type.getPropertyClass().cast(prop));
		}
	}
	
	private <T extends EntityProperty> void put(EntityPropertyType<T> type, T entityProperty) {
		singleProperties.put(type, entityProperty);
		propertyLists.get(type).add(entityProperty);
	}
	
	interface Action<T extends EntityProperty> {
		void act(T property);
	}
	
	interface Evaluator<T extends EntityProperty, R> {
		R getValue(T property);
	}
	
	public <T extends EntityProperty> void forAll(EntityPropertyType<T> type, Action<T> action) {
		//noinspection unchecked
		Array<? extends T> properties = (Array<? extends T>)propertyLists.get(type);
		for(T prop: properties)
			action.act(prop);
	}
	public <T extends EntityProperty, ST extends T> void forAll(EntityPropertyType<T> type, Class<ST> subClass, Action<ST> action) {
		//noinspection unchecked
		Array<? extends T> properties = (Array<? extends T>)propertyLists.get(type);
		for(T prop: properties) {
			if(subClass.isAssignableFrom(prop.getClass()))
				action.act(subClass.cast(prop));
		}
	}
	
	public <T extends EntityProperty> boolean fromAll(EntityPropertyType<T> type, Evaluator<T, Boolean> boolMaker) {
		//noinspection unchecked
		Array<? extends T> properties = (Array<? extends T>)propertyLists.get(type);
		boolean done = false;
		for(T prop: properties)
			done = boolMaker.getValue(prop) || done;
		
		return done;
	}
	
	public <T extends EntityProperty, ST extends T> boolean fromAll(EntityPropertyType<T> type, Class<ST> subClass, Evaluator<ST, Boolean> boolMaker) {
		//noinspection unchecked
		Array<? extends T> properties = (Array<? extends T>)propertyLists.get(type);
		boolean done = false;
		for(T prop: properties)
			if(subClass.isAssignableFrom(prop.getClass()))
				done = boolMaker.getValue(subClass.cast(prop)) || done;
		
		return done;
	}
	
	public <T extends EntityProperty> T getSingleProp(EntityPropertyType<T> type) {
		//noinspection unchecked
		return (T) singleProperties.get(type);
	}
	public <T extends EntityProperty> T getSingleProp(EntityPropertyType<? super T> type, Class<T> asClass) {
		//noinspection unchecked
		return (T) singleProperties.get(type);
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return world; }
	
	@Nullable @Override
	public Level getLevel() { return null; }
	
	@NotNull @Override
	public Rectangle getBounds() {
		Vector2 size = getSize();
		return new Rectangle(x, y, size.x, size.y);
	}
	
	@Override
	public Vector2 getSize() { return getSingleProp(EntityPropertyType.Renderer).getSize().scl(1f/Tile.SIZE); }
	
	@Nullable @Override
	public ServerLevel getServerLevel() {
		return null;
	}
	
	@Override
	public void update(float delta) {
		forAll(EntityPropertyType.Updatable, updater -> updater.update(delta, this));
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		forAll(EntityPropertyType.Renderer, renderer -> renderer.drawSprite((x - posOffset.x) * Tile.SIZE, (y - posOffset.y) * Tile.SIZE, batch, delta, this));
	}
	
	@Override
	public boolean isPermeableBy(Entity entity) {
		return fromAll(EntityPropertyType.Tangibility, tangibility -> tangibility.isPermeableBy(entity, this));
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) {
		return fromAll(EntityPropertyType.InteractAction, interaction -> interaction.interactWith(player, heldItem, this));
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int dmg) {
		return fromAll(EntityPropertyType.AttackResult, attackResult -> attackResult.attackedBy(obj, item, dmg, this));
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		return fromAll(EntityPropertyType.TouchListener, touchListener -> touchListener.touchedBy(entity, true, this));
	}
	
	@Override
	public void touching(Entity entity) {
		forAll(EntityPropertyType.TouchListener, touchListener -> touchListener.touchedBy(entity, false, this));
	}
	
	@Override
	public Tag getTag() {
		return null;
	}
	
	public float getZ() { return z; }
	protected void setZ(float z) { this.z = z; }
	
	
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
		if(moved) {
			moveTo(level, x+movement.x, y+movement.y);
			// TODO send movement updates to clients
			//if(level instanceof ServerLevel)
			//	level.getWorld().getSender().sendData(new Movement(getId(), level.getDepth(), new Vector3(getPosition(), getZ())));
		}
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
			if(getServerLevel() != null)
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
		
		Array<miniventure.game.world.entity.Entity> newEntities = level.getOverlappingEntities(newRect);
		newEntities.removeAll(level.getOverlappingEntities(oldRect), true); // because the "old rect" entities are never added in the first place, we don't need to worry about this entity being included in this list, and accidentally interacting with itself.
		for(miniventure.game.world.entity.Entity entity: newEntities) {
			if(getServerLevel() != null)
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
		/*boolean changedChunk =
			!level.equals(getLevel()) ||
			Chunk.getCoord(x) != Chunk.getCoord(this.x) ||
			Chunk.getCoord(y) != Chunk.getCoord(this.y);
		*/
		this.x = x;
		this.y = y;
		
		if(level == getLevel())
			level.entityMoved(this);
		else
			world.setEntityLevel(this, level);
	}
	public void moveTo(@NotNull Level level, float x, float y, float z) {
		moveTo(level, x, y);
		setZ(z);
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
}
