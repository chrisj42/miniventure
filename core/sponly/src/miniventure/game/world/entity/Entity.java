package miniventure.game.world.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import miniventure.game.core.GameCore;
import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.blinker.Blinker;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.entity.property.RenderProperty;
import miniventure.game.world.management.Level;
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

public abstract class Entity implements WorldObject, RenderProperty {
	
	// @NotNull private final WorldManager world;
	@NotNull public final Level level;
	
	// private final EntityTag tag;
	private final int eid;
	float x, y, z = 0;
	
	// private RenderProperty baseRenderer; // separate so that this can be changed without invalidating all modifiers
	@NotNull
	private RenderProperty entityRenderer;
	
	// for entities updated locally (called by both server and client for different entities)
	// "local" refers to if the entity exists only locally, as opposed to being synced across clients.
	protected Entity(@NotNull EntitySpawn info) {
		this.entityRenderer = this;
		this.level = info.getLevel();
		this.x = info.getX();
		this.y = info.getY();
		EntitySpawn.free(info);
		eid = level.addEntity(this);
		// this.tag = new EntityTag(eid);
	}
	
	protected Entity(@NotNull Level level, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		this.entityRenderer = this;
		this.level = level;
		
		modifier.act(allData);
		
		SerialHashMap data = allData.get("e");
		x = data.get("x", Float::parseFloat);
		y = data.get("y", Float::parseFloat);
		// z = data.get("z", Float::parseFloat);
		
		eid = level.addEntity(this);
	}
	
	public EntityDataSet save() {
		EntityDataSet allData = new EntityDataSet();
		SerialHashMap data = new SerialHashMap();
		data.add("x", x);
		data.add("y", y);
		// data.add("z", z);
		
		allData.put("e", data);
		return allData;
	}
	
	public int getId() { return eid; }
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@Override @NotNull
	public Level getLevel() { return level; }
	
	// public abstract boolean isMob();
	public boolean isFloating() { return false; }
	
	/// this is called only to remove an entity completely from the game, not to change levels.
	public void remove() {
		level.removeEntity(this);
	}
	
	public void update(float delta) {
		if(isFloating()) return; // floating entities don't interact
		
		Array<WorldObject> objects = new Array<>();
		objects.addAll(level.getOverlappingEntities(getBounds(), this));
		// we don't want to trigger things like getting hurt by lava until the entity is actually *in* the tile, so we'll only consider the closest one to be "touching".
		Tile tile = level.getTile(getBounds());
		if(tile != null) objects.add(tile);
		
		for(WorldObject obj: objects)
			obj.touching(this);
		
		// get the entity back on the map if they somehow end up on a null tile
		if(tile == null)
			moveTo(level.getClosestTile(getBounds()).getCenter());
	}
	
	/*public void setRenderer(@NotNull EntityRenderer renderer) {
		this.renderer = renderer;
		if(blinker != null)
			blinker.setRenderer(renderer);
	}*/
	
	// TODO FIXME entity rendering modifiers were never finished
	public void setBlinker(@Nullable Color blinkColor, float initialDuration, boolean blinkFirst, Blinker blinker) {
		// this.blinker = new BlinkRenderer(renderer, blinkColor, initialDuration, blinkFirst, blinker);
	}
	
	/*protected void setRenderer(RenderProperty renderer) {
		this.baseRenderer = renderer;
		if(entityRenderer == null)
			entityRenderer = renderer;
	}
	
	protected void modifyRenderer(RenderModifier renderer) { modifyRenderer(renderer, -1); }
	protected void modifyRenderer(RenderModifier renderer, float duration) {
		entityRenderer = 
	}*/
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		if(entityRenderer.shouldRender())
			entityRenderer.render(getSprite(), (x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE, batch, 1f);
		
		if(GameCore.debugBounds && !(this instanceof Particle)) {
			Rectangle rect = getBounds();
			rect.x = (rect.x - posOffset.x) * Tile.SIZE;
			rect.y = (rect.y - posOffset.y) * Tile.SIZE;
			rect.width *= Tile.SIZE;
			rect.height *= Tile.SIZE;
			MyUtils.drawRect(rect, 1, Color.BLACK, batch);
		}
	}
	
	protected abstract TextureHolder getSprite();
	
	@Override
	public boolean shouldRender() { return true; }
	
	@Override
	public final RenderProperty getRenderPipe() { return null; }
	
	@Override
	public void render(TextureHolder sprite, float x, float y, SpriteBatch batch, float drawableHeight) {
		if(drawableHeight == 1)
			batch.draw(sprite.texture, x, y);
		else
			batch.draw(sprite.texture.split(sprite.width, (int) (sprite.height * drawableHeight))[0][0], x, y);
	}
	
	protected Rectangle getUnscaledBounds() {
		TextureHolder sprite = getSprite();
		return new Rectangle(x, y, sprite == null ? 0 : sprite.width, sprite == null ? 0 : sprite.height);
	}
	
	public Vector3 getLocation() { return new Vector3(x, y, z); }
	
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
		// Level level = getLevel();
		// if(level == null) return false; // can't move if you're not in a level...
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
	
	void touchTile(Tile tile) {
		tile.touchedBy(this); // NOTE: I can see this causing an issue if you move too fast; it will "touch" tiles that could be far away, if the player will move there next frame.
	}
	
	void touchEntity(Entity entity) {
		if(!entity.touchedBy(this))
			this.touchedBy(entity); // to make sure something has a chance to happen, but it doesn't happen twice.
	}
	
	public void moveTo(@NotNull Vector2 pos) { moveTo(pos.x, pos.y); }
	public void moveTo(@NotNull Vector3 pos) { moveTo(pos.x, pos.y, pos.z); }
	public void moveTo(float x, float y) { moveTo(x, y, this.z); }
	public void moveTo(float x, float y, float z) {
		// this method doesn't care where you end up; ie doesn't check for collisions.
		// it also doesn't bother with clamping if there is no level.
		
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		Vector2 size = getSize();
		x = Math.min(x, level.getWidth() - size.x);
		y = Math.min(y, level.getHeight() - size.y);
		
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void moveTo(@NotNull Tile tile) {
		Vector2 pos = tile.getCenter();
		pos.sub(getSize().scl(0.5f));
		moveTo(pos);
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
	
	
	/*public static class EntityTag implements Tag<Entity> {
		public final int eid;
		
		private EntityTag() { this(0); }
		public EntityTag(int eid) {
			this.eid = eid;
		}
		
		@Override
		public Entity getObject(WorldManager world) { return world.getEntity(eid); }
	}*/
	
	// @Override
	// public EntityTag getTag() { return tag; }
	
	@Override
	public String toString() { return Integer.toHexString(super.hashCode())+'_'+getClass().getSimpleName()+'-'+eid; }
	
	public int superHash() { return super.hashCode(); }
	
	public String serialize() { return serialize(this); }
	
	public static String serialize(Entity e) {
		EntityDataSet data = e.save();
		
		SerialHashMap allData = new SerialHashMap();
		for(Entry<String, SerialHashMap> entry: data.entrySet())
			allData.put(entry.getKey(), entry.getValue().serialize());
		
		allData.add("class", e.getClass().getCanonicalName().replace(Entity.class.getPackage().getName()+".", ""));
		
		return allData.serialize();
	}
	
	public static Entity deserialize(@NotNull Level level, String data, @NotNull Version version) {
		SerialHashMap allData = new SerialHashMap(data);
		
		String entityType = allData.remove("class");
		
		EntityDataSet map = new EntityDataSet();
		for(Entry<String, String> entry: allData.entrySet()) {
			map.put(entry.getKey(), new SerialHashMap(entry.getValue()));
		}
		
		Entity entity = null;
		
		try {
			Class<?> clazz = Class.forName(Entity.class.getPackage().getName()+'.'+entityType);
			
			Class<? extends Entity> entityClass = clazz.asSubclass(Entity.class);
			
			entity = deserialize(level, entityClass, map, version);
			
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	public static <T extends Entity> T deserialize(@NotNull Level level, Class<T> clazz, EntityDataSet data, @NotNull Version version) {
		T newEntity = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(Level.class, EntityDataSet.class, Version.class, ValueAction.class);
			constructor.setAccessible(true);
			newEntity = constructor.newInstance(level, data, version, (ValueAction<EntityDataSet>)(allData -> {}));
		} catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		
		return newEntity;
	}
}
