package miniventure.game.world;

import miniventure.game.client.ClientWorld;

import org.jetbrains.annotations.NotNull;

public class ClientLevel extends Level {
	
	@NotNull private ClientWorld world;
	
	public ClientLevel(@NotNull ClientWorld world, int depth, int width, int height) {
		super(world, depth, width, height);
		this.world = world;
	}
	
	@Override @NotNull
	public ClientWorld getWorld() { return world; }
	
}
