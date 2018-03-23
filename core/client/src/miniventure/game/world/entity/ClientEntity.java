package miniventure.game.world.entity;

import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.world.ClientLevel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	private final boolean permeable;
	
	public ClientEntity(int eid, boolean permeable, @NotNull EntityRenderer renderer) {
		super(ClientCore.getWorld(), eid);
		this.permeable = permeable;
		setRenderer(renderer);
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
	@Override
	public boolean isPermeable() { return permeable; }
	
	@Override
	public void update(float delta) {}
	
	@Override
	public boolean move(float x, float y) { return move(x, y, this.z); }
	
	@Override
	public boolean move(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return true;
	}
	
	
}
