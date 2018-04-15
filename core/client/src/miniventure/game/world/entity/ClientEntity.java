package miniventure.game.world.entity;

import miniventure.game.GameProtocol.EntityAddition;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Mob;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	private final boolean permeable;
	private final String descriptor;
	private final boolean cutHeight;
	
	private ClientEntity(int eid, boolean permeable, @NotNull EntityRenderer renderer, String descriptor, boolean cutHeight) {
		super(ClientCore.getWorld(), eid);
		this.permeable = permeable;
		this.descriptor = descriptor;
		this.cutHeight = cutHeight;
		setRenderer(renderer);
	}
	public ClientEntity(EntityAddition data) {
		this(data.eid, data.permeable, EntityRenderer.deserialize(data.spriteUpdate.rendererData), data.descriptor, data.cutHeight);
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
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
	public boolean move(float xd, float yd, float zd) { return move(xd, yd, zd, false); }
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
	public String toString() { return super.toString()+"-" + descriptor/* + " at " + getPosition(true)*/; }
}
