package miniventure.game.world.entitynew;

import java.util.HashMap;

import miniventure.game.api.APIObject;
import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entitynew.property.*;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Entity extends APIObject<EntityType, EntityProperty> implements WorldObject {
	
	/*
		So the problem with entities is that they will always have instance-specific data, that varies for each type; unlike tiles, where all tiles start off with the same state. But with entities, it is often wanted to create it with specific data. Tiles always start with the same data.
		
		 Honestly... the issue is here that entities are just a lot more versatile than tiles. There are a lot more possible configurations, and they vary for each entity. That's why entities had constructors, and tiles barely did, originally.
		 
		 So, how to fix this... maybe I can have the Entity class be extended, but only for the purposes of entity creation? I mean... It's basically like having the constructor in the entity type, except that there's inheritance. Which could be cool, or might not be. Meh, I'll just stick with keeping the constructors here for now.
		 If I need extension classes, I can make them; for example, the player. The PLAYER entity type will then just return a new player instance rather than an entity, I guess.
		 Or, maybe the player will be an entirely separate object that just has a field of type entity, idk. :P
		 Naw, I want to keep things together.
	 */
	
	@NotNull private final EntityType type;
	@NotNull private final HashMap<Class<? extends EntityProperty>, InstanceData> dataMap;
	
	private float x, y, z;
	
	private int eid;
	private boolean hasID = false;
	
	Entity(@NotNull EntityType type) {
		this.type = type;
		this.dataMap = type.createDataObjectMap();
		// to get the InstanceData instances, I need to ask the type, and pass the data string if I have one already.
		// if I pass data in, it will get the map as if I didn't, but then initialize everything with the data afterward.
		
		// solution to bounce property needing data but extending lifetime property which also needs data: have bounce property data type extend life property data type..? Meh, I really don't like that...
		// maybe I'll just have to pass around the map, even though that could go wrong, technically.
		// entity type class makes a map, and passes it around to each property. each property will add an entry for it's own class if it wants to.
		// to fetch the data object, the property asks the entity object, passing its class type and the class type of its data. The unchecked cast to the given InstanceData subclass will occur in the method.
	}
	Entity(@NotNull EntityType type, @NotNull String[] data) {
		this.type = type;
		String[] leftoverData = parseCoords(data);
		dataMap = type.createDataObjectMap(leftoverData);
	}
	Entity(@NotNull EntityType type, @NotNull String[] data, int eid) {
		this(type, data);
		this.eid = eid;
		hasID = true;
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
	
	@Override @NotNull
	public EntityType getType() { return type; }
	
	@Override
	public String getData(Class<? extends EntityProperty> property, EntityType type, int propDataIdx) {
		type.checkDataAccess(property, propDataIdx);
		return dataMap.get(property).serializeData()[propDataIdx];
	}
	
	@Override
	public void setData(Class<? extends EntityProperty> property, EntityType type, int propDataIdx, String data) {
		type.checkDataAccess(property, propDataIdx);
		String[] dataArray = dataMap.get(property).serializeData();
		dataArray[propDataIdx] = data;
		dataMap.get(property).parseData(dataArray);
	}
	
	// not my best work, here... it relies on generally being called only where it is known how data is stored... ugh... I really suck at this sometimes...
	public <P extends EntityProperty, T extends DataCarrier<D>, D extends InstanceData> D getDataObject(Class<T> dataClass, Class<P> propertyClass) {
		//noinspection unchecked
		return (D) dataMap.get(propertyClass);
	}
	
	@NotNull
	public String[] getData(boolean includeCoords) {
		String[] data = new String[type.getDataLength()+(includeCoords?3:0)];
		
		for(Class<? extends EntityProperty> propClass: dataMap.keySet()) {
			String[] propData = dataMap.get(propClass).serializeData();
			int offset = type.getPropDataIndex(propClass);
			System.arraycopy(propData, 0, data, offset+(includeCoords?3:0), propData.length);
		}
		
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
	public Rectangle getBounds() {
		Vector2 size = type.getProp(SizeProperty.class).getSize();
		return new Rectangle(x, y, size.x, size.y);
	}
	
	@Override
	public void update(float delta) {
		type.getProp(UpdateProperty.class).update(this, delta);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		type.getProp(RenderProperty.class).render(this, delta, batch, (x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE);
	}
	
	public float getZ() { return z; }
	public void setZ(float z) { this.z = z; }
	
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
		if(moved) {
			moveTo(level, x+movement.x, y+movement.y);
			type.getProp(MovementListener.class).entityMoved(this, movement);
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
		if(!hasID) {
			eid = level.getWorld().generateEntityID(this);
			hasID = true;
		}
		
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
	public final boolean isPermeableBy(Entity entity) {
		return type.getProp(PermeableProperty.class).isPermeable(this, entity, true);
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) {
		return type.getProp(InteractionListener.class).interact(player, heldItem, this);
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int dmg) {
		return type.getProp(AttackListener.class).attackedBy(obj, item, dmg, this);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		return type.getProp(TouchListener.class).entityTouched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		type.getProp(TouchListener.class).entityTouched(this, entity, false);
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof Entity && ((Entity)other).eid == eid; }
	
	@Override
	public int hashCode() { return eid; }
	
	@Override
	public String toString() {
		return type + " Entity";
	}
}
