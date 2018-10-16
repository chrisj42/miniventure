package miniventure.game.world.tile;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.CacheTag;
import miniventure.game.world.tile.data.DataMap;

import org.jetbrains.annotations.NotNull;

public class UpdateManager {
	
	/*
		Update Actions have:
			- an action they do on update
			- a period to wait before that update happens
			- a way to signal what that wait period is, that is only requested once per update cycle
	 */
	
	interface UpdateAction {
		void update(@NotNull Tile tile, FetchFunction<String> dataCacheFetcher, ValueFunction<String> dataCacheSetter);
		
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
		// if playing an exit animation, then don't update the tile.
		TransitionManager man = tileType.getTileType(tile.getWorld()).getRenderer().transitionManager;
		if(man.playingExitAnimation(tile))
			return man.tryFinishAnimation(tile);
		
		float minWait = 0;
		DataMap dataMap = tile.getDataMap(tileType);
		float[] deltas = dataMap.getOrDefaultAndPut(CacheTag.UpdateTimers, new float[actions.length]);
		String[] datas = dataMap.getOrDefaultAndPut(CacheTag.UpdateActionCaches, new String[actions.length]);
		for(int i = 0; i < actions.length; i++) {
			if(!actions[i].canUpdate(tile))
				deltas[i] = 0;
			else if(deltas[i] == 0)
				deltas[i] = actions[i].getDelta(tile);
			else {
				deltas[i] -= delta;
				if(deltas[i] <= 0) {
					UpdateAction action = actions[i];
					final int idx = i;
					action.update(tile, () -> datas[idx], value -> datas[idx] = value);
					deltas[i] = 0;
				}
			}
			
			if(minWait == 0)
				minWait = deltas[i];
			else if(deltas[i] != 0)
				minWait = Math.min(minWait, deltas[i]);
		}
		
		return minWait;
	}
}
