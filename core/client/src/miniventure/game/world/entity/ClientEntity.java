package miniventure.game.world.entity;

import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.util.FrameBlinker;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.WorldObject;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	private final boolean permeable;
	private final String descriptor;
	
	public ClientEntity(int eid, boolean permeable, @NotNull EntityRenderer renderer, String descriptor) {
		super(ClientCore.getWorld(), eid);
		this.permeable = permeable;
		this.descriptor = descriptor;
		setRenderer(renderer);
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public boolean isPermeable() { return permeable; }
	
	// power = health percent, loosely
	public void hurt(WorldObject source, float power) {
		setBlinker(0.5f, true, new FrameBlinker(5, 1, false));
	}
	
	public boolean move(Vector2 pos, boolean validate) { return move(pos.x, pos.y, validate); }
	public boolean move(Vector3 pos, boolean validate) { return move(pos.x, pos.y, pos.z, validate); }
	public boolean move(float x, float y, boolean validate) { return move(x, y, this.z, validate); }
	public boolean move(float x, float y, float z) { return move(x, y, z, false); }
	public boolean move(float x, float y, float z, boolean validate) {
		if(validate)
			return super.move(x, y, z);
		
		moveTo(new Vector3(x, y, z).add(getLocation()));
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
