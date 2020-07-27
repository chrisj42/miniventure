package miniventure.game.world.entity;

import miniventure.game.core.ClientCore;
import miniventure.game.network.GameProtocol.EntityAddition;
import miniventure.game.util.MyUtils;
import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.util.pool.RectPool;
import miniventure.game.util.pool.VectorPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.particle.ClientParticle;
import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.SwimAnimation;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	private final boolean permeable;
	private final String descriptor;
	private final boolean cutHeight;
	private final boolean canFloat;
	
	public ClientEntity(EntityAddition data) {
		super(ClientCore.getWorld(), data.eid, data.positionUpdate);
		this.permeable = data.permeable;
		this.descriptor = data.descriptor;
		this.cutHeight = data.cutHeight;
		this.canFloat = data.canFloat;
		setRenderer(ClientEntityRenderer.deserialize(data.spriteUpdate.rendererData));
	}
	
	// for locally updated entities. Assumes traits of a particle.
	protected ClientEntity() {
		super(ClientCore.getWorld(), true);
		permeable = true;
		canFloat = true;
		cutHeight = false;
		descriptor = getClass().getSimpleName();
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		super.render(batch, delta, posOffset);
		float drawableHeight = 1;
		if(!canFloat) {
			ClientTile closest = (ClientTile) getClosestTile();
			if(closest != null) {
				SwimAnimation swimAnimation = closest.getType().getSwimAnimation();
				if(swimAnimation != null) {
					Rectangle bounds = getBounds();
					Vector2 pos = bounds.getCenter(VectorPool.POOL.obtain()).sub(posOffset).sub(0, bounds.height / 2).scl(Tile.SIZE);
					swimAnimation.drawSwimAnimation(batch, pos, getWorld());
					drawableHeight = swimAnimation.drawableHeight;
					RectPool.POOL.free(bounds);
				}
			}
		}
		getRenderer().render((x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE, batch, drawableHeight);
		
		if(ClientCore.debugBounds && !(this instanceof ClientParticle)) {
			Rectangle rect = getBounds();
			rect.x = (rect.x - posOffset.x) * Tile.SIZE;
			rect.y = (rect.y - posOffset.y) * Tile.SIZE;
			rect.width *= Tile.SIZE;
			rect.height *= Tile.SIZE;
			MyUtils.drawRect(rect, 1, Color.BLACK, batch);
			RectPool.POOL.free(rect);
		}
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public boolean isMob() { return cutHeight; } // this is probably a bad idea but currently it is exactly the value I'm looking for...
	@Override
	public boolean isFloating() { return canFloat; }
	
	@NotNull @Override
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		if(cutHeight) bounds.height = Mob.shortenSprite(bounds.height);
		return bounds;
	}
	
	@Override
	public boolean isPermeable() { return permeable; }
	
	// power = health percent, loosely
	public void hurt(WorldObject source, float power) {
		setBlinker(Color.RED, Mob.HURT_COOLDOWN, true, new FrameBlinker(5, 1, false));
	}
	
	public boolean move(boolean validate, Vector2 moveDist) { return move(validate, moveDist.x, moveDist.y); }
	public boolean move(boolean validate, Vector3 moveDist) { return move(validate, moveDist.x, moveDist.y, moveDist.z); }
	public boolean move(boolean validate, float xd, float yd) { return move(validate, xd, yd, 0); }
	@Override
	public boolean move(float xd, float yd, float zd) { return move(false, xd, yd, zd); }
	
	@Override void touchTile(Tile tile) {}
	@Override void touchEntity(Entity entity) {}
	
	public boolean move(boolean validate, float xd, float yd, float zd) {
		if(validate)
			return super.move(xd, yd, zd);
		
		moveTo(xd + x, yd + y, zd + z);
		return true;
	}
	
	@Override
	public String toString() { return super.toString()+'-'+descriptor/* + " at " + getPosition(true)*/; }
}
