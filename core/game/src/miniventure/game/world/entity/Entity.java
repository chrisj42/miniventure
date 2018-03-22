package miniventure.game.world.entity;

import miniventure.game.item.Item;
import miniventure.game.util.Blinker;
import miniventure.game.world.Level;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.EntityRenderer.BlinkRenderer;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Entity implements WorldObject {
	
	@NotNull private final WorldManager world;
	
	private int eid;
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
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		renderer.render((x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE, batch, delta);
	}
	
	protected Rectangle getUnscaledBounds() {
		Vector2 size = renderer.getSize();
		return new Rectangle(x, y, size.x, size.y);
	}
	
	public Vector3 getLocation() { return new Vector3(x, y, z); }
		
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
	public abstract boolean move(float xd, float yd);
	public abstract boolean move(float xd, float yd, float zd);
	
	public void moveTo(@NotNull Level level, @NotNull Vector2 pos) { moveTo(level, pos.x, pos.y); }
	public void moveTo(@NotNull Level level, float x, float y) {
		if(level == getLevel() && x == this.x && y == this.y) return; // no action or updating required.
		
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
	public final boolean isPermeableBy(Entity entity) { return isPermeableBy(entity, true); }
	
	public boolean isPermeableBy(Entity entity, boolean delegate) {
		if(delegate)
			return entity.isPermeableBy(this, false);
		return false;
	}
	
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
	public String toString() { return getClass().getSimpleName(); }
}
