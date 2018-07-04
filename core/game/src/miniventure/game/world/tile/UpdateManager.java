package miniventure.game.world.tile;

import java.util.Arrays;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.VoidMonoFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.DataTag;

import org.jetbrains.annotations.NotNull;

public class UpdateManager {
	
	@FunctionalInterface
	interface UpdateAction {
		float update(@NotNull Tile tile, float delta, ValueFunction<Float> deltaCacheFetcher, VoidMonoFunction<Float> deltaCacheSetter);
	}
	
	private final TileTypeEnum tileType;
	private final UpdateAction[] actions;
	
	public UpdateManager(@NotNull TileTypeEnum tileType, UpdateAction... actions) {
		this.tileType = tileType;
		this.actions = actions;
	}
	
	public float update(@NotNull Tile tile, float delta) {
		float minWait = 0;
		DataMap dataMap = tile.getDataMap(tileType);
		Float[] deltas = dataMap.getOrDefaultAndPut(DataTag.UpdateTimers, new Float[actions.length]);
		for(int i = 0; i < actions.length; i++) {
			UpdateAction action = actions[i];
			final int idx = i;
			float wait = action.update(tile, delta, () -> deltas[idx], value -> deltas[idx] = value);
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
