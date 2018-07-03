package miniventure.game.world.tile;

import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

public class UpdateManager {
	
	@FunctionalInterface
	interface UpdateAction {
		float update(@NotNull Tile tile, float delta);
	}
	
	
	private final TileTypeEnum tileType;
	private final UpdateAction[] actions;
	
	public UpdateManager(@NotNull TileTypeEnum tileType, UpdateAction... actions) {
		this.tileType = tileType;
		this.actions = actions;
	}
	
	public float update(@NotNull Tile tile, float delta) {
		float minWait = 0;
		for(UpdateAction action: actions) {
			float wait = action.update(tile, delta);
			if(wait > 0) {
				if(minWait == 0)
					minWait = wait;
				else
					minWait = Math.min(minWait, wait);
			}
		}
		
		TransitionManager man = tileType.getTileType(tile.getWorld()).getRenderer().transitionManager;
		if(man.playingAnimation(tile)) {
			float wait = man.getTimeRemaining(tile);
			if(wait > 0)
				minWait = Math.min(minWait, wait);
		}
		
		return minWait;
	}
}
