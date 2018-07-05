package miniventure.game.world.entity;

import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.tile.SwimAnimation;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.data.PropertyTag;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	private final boolean particle;
	private final boolean permeable;
	private final String descriptor;
	private final boolean cutHeight;
	
	public ClientEntity(EntityAddition data) {
		super(ClientCore.getWorld(), data.eid);
		this.particle = data.particle;
		this.permeable = data.permeable;
		this.descriptor = data.descriptor;
		this.cutHeight = data.cutHeight;
		setRenderer(EntityRenderer.deserialize(data.spriteUpdate.rendererData));
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		super.render(batch, delta, posOffset);
		float drawableHeight = 1;
		if(!particle) {
			Tile closest = getClosestTile();
			if(closest != null) {
				SwimAnimation swimAnimation = closest.getType().getPropertyOrDefault(PropertyTag.Swim, null);
				if(swimAnimation != null) {
					Vector2 pos = getCenter().sub(posOffset).sub(0, getSize().y / 2).scl(Tile.SIZE);
					swimAnimation.drawSwimAnimation(batch, pos, getWorld());
					drawableHeight = swimAnimation.drawableHeight;
				}
			}
		}
		getRenderer().render((x-posOffset.x) * Tile.SIZE, (y+z - posOffset.y) * Tile.SIZE, batch, drawableHeight);
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public boolean isMob() { return cutHeight; } // this is probably a bad idea but currently it is exactly the value I'm looking for...
	@Override
	public boolean isParticle() { return particle; }
	
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
		setBlinker(0.5f, true, new FrameBlinker(5, 1, false));
	}
	
	public boolean move(Vector2 moveDist, boolean validate) { return move(moveDist.x, moveDist.y, validate); }
	public boolean move(Vector3 moveDist, boolean validate) { return move(moveDist.x, moveDist.y, moveDist.z, validate); }
	public boolean move(float xd, float yd, boolean validate) { return move(xd, yd, this.z, validate); }
	@Override
	public boolean move(float xd, float yd, float zd) { return move(xd, yd, zd, false); }
	
	@Override void touchTile(Tile tile) {}
	@Override void touchEntity(Entity entity) {}
	
	public boolean move(float xd, float yd, float zd, boolean validate) {
		if(validate)
			return super.move(xd, yd, zd);
		
		moveTo(new Vector3(xd, yd, zd).add(getLocation()));
		return true;
	}
	
	public void moveTo(Vector2 pos) { moveTo(pos.x, pos.y); }
	public void moveTo(Vector3 pos) { moveTo(pos.x, pos.y, pos.z); }
	public void moveTo(float x, float y) { moveTo(x, y, this.z); }
	public void moveTo(float x, float y, float z) {
		this.z = z;
		ClientLevel level = getLevel();
		if(level == null) {
			this.x = x;
			this.y = y;
		}
		else
			moveTo(level, x, y);
	}
	
	@Override
	public String toString() { return super.toString()+'-'+descriptor/* + " at " + getPosition(true)*/; }
}
