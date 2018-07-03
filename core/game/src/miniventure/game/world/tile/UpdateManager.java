package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

public class UpdateManager {
	
	@FunctionalInterface
	interface UpdateAction {
		float update(@NotNull Tile tile, float delta);
	}
	
	
	private final UpdateAction[] actions;
	
	public UpdateManager(UpdateAction... actions) {
		this.actions = actions;
	}
	
	public float update(@NotNull Tile tile, float delta) {
		
	}
}
