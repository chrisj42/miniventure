package miniventure.game.world.entitynew;

import org.jetbrains.annotations.NotNull;

public class Player extends Entity {
	
	public Player(@NotNull EntityType type) {
		super(type);
	}
	
	public Player(@NotNull EntityType type, @NotNull String[] data) {
		super(type, data);
	}
	
	public Player(@NotNull EntityType type, @NotNull String[] data, int eid) {
		super(type, data, eid);
	}
	
	
}
