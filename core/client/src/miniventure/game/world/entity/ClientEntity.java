package miniventure.game.world.entity;

import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;
import miniventure.game.world.ClientLevel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntity extends Entity {
	
	public ClientEntity(int eid, @NotNull EntityRenderer renderer) {
		super(ClientCore.getWorld(), eid);
		setRenderer(renderer);
	}
	
	@NotNull @Override
	public ClientWorld getWorld() { return (ClientWorld) super.getWorld(); }
	
	@Nullable @Override
	public ClientLevel getLevel() { return (ClientLevel) super.getLevel(); }
	
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
