package miniventure.game.world.tile;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.tile.ServerTileType.P;

import org.jetbrains.annotations.NotNull;

public class UpdateManager implements TileProperty {
	
	/*
		Update Actions have:
			- an action they do on update
			- a period to wait before that update happens
			- a way to signal what that wait period is, that is only requested once per update cycle
	 */
	
	interface UpdateAction {
		void update(@NotNull ServerTile tile, FetchFunction<String> dataCacheFetcher, ValueAction<String> dataCacheSetter);
		
		boolean canUpdate(@NotNull ServerTile tile);
		
		float getDelta(@NotNull ServerTile tile);
	}
	
	private final TileTypeEnum tileType;
	private final UpdateAction[] actions;
	
	public UpdateManager(@NotNull TileTypeEnum tileType, UpdateAction... actions) {
		this.tileType = tileType;
		this.actions = actions;
	}
	
	@Override
	public void registerDataTypes(TileType tileType) {
		if(doesUpdate()) {
			tileType.registerData(TileDataTag.LastUpdate);
			tileType.registerData(TileDataTag.UpdateTimers);
			tileType.registerData(TileDataTag.UpdateActionCaches);
		}
	}
	
	public boolean doesUpdate() { return actions.length > 0; }
	
	public float update(@NotNull ServerTile tile, float delta) {
		// if playing an exit animation, then don't update the tile.
		TransitionManager man = ServerTileType.get(tileType, P.TRANS);
		if(man.playingAnimation(tile))
			return man.tryFinishAnimation(tile);
		
		float minWait = 0;
		TileTypeDataMap dataMap = tile.getDataMap(tileType);
		float[] deltas = dataMap.getOrDefaultAndPut(TileDataTag.UpdateTimers, new float[actions.length]);
		String[] datas = dataMap.getOrDefaultAndPut(TileDataTag.UpdateActionCaches, new String[actions.length]);
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
