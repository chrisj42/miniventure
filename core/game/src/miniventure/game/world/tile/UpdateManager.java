package miniventure.game.world.tile;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.VoidMonoFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;
import miniventure.game.world.tile.data.DataTag;

import org.jetbrains.annotations.NotNull;

public class UpdateManager {
	
	/*
		Update Actions have:
			- an action they do on update
			- a period to wait before that update happens
			- a way to signal what that wait period is, that is only requested once per update cycle
	 */
	
	interface UpdateAction {
		void update(@NotNull Tile tile, ValueFunction<String> dataCacheFetcher, VoidMonoFunction<String> dataCacheSetter);
		
		boolean canUpdate(@NotNull Tile tile);
		
		float getDelta(@NotNull Tile tile);
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
		String[] datas = dataMap.getOrDefaultAndPut(DataTag.UpdateActionCaches, new String[actions.length]);
		for(int i = 0; i < actions.length; i++) {
			float wait;
			if(!actions[i].canUpdate(tile)) {
				wait = 0f;
			}
			else if(deltas[i] == 0f) {
				wait = actions[i].getDelta(tile);
			}
			else {
				deltas[i] -= delta;
				if(deltas[i] <= 0) {
					UpdateAction action = actions[i];
					final int idx = i;
					action.update(tile, () -> datas[idx], value -> datas[idx] = value);
					wait = action.getDelta(tile);
				} else wait = 0;
			}
			deltas[i] = wait;
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
