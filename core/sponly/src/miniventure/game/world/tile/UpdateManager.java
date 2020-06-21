package miniventure.game.world.tile;

import miniventure.game.world.tile.Tile.TileContext;

import org.jetbrains.annotations.NotNull;

public class UpdateManager implements TileProperty {
	
	public static final UpdateManager NONE = new UpdateManager(null);
	
	/*
		Update Actions have:
			- an action they do on update
			- a period to wait before that update happens
			- a way to signal what that wait period is, that is only requested once per update cycle
	 */
	
	interface UpdateAction {
		float update(TileContext context);
		
		// boolean canUpdate(@NotNull Tile tile);
		
		// float getDelta(@NotNull Tile tile);
	}
	
	// private final UpdateAction[] actions;
	private final UpdateAction action;
	
	public UpdateManager(UpdateAction action) {
		this.action = action;
	}
	
	@Override
	public void registerDataTags(TileType tileType) {
		if(action != null) {
			// tileType.addDataTag(TileDataTag.LastUpdate);
			tileType.addDataTag(TileDataTag.UpdateTimer);
		}
	}
	
	public float update(@NotNull Tile.TileContext context, float delta) {
		
		if(action == null)
			return 0;
		
		float delayOriginal = context.getData(TileDataTag.UpdateTimer);
		float delayLeft = delayOriginal - delta;
		
		if(delayLeft <= 0)
			delayLeft = action.update(context);
		
		context.setData(TileDataTag.UpdateTimer, delayLeft);
		return delayLeft;
		
		// float minWait = 0;
		// TileTypeDataMap dataMap = context.getDataMap(tileType);
		// float[] deltas = dataMap.getOrDefaultAndPut(TileDataTag.UpdateTimers, new float[actions.length]);
		// String[] datas = dataMap.getOrDefaultAndPut(TileDataTag.UpdateActionCaches, new String[actions.length]);
		/*for(int i = 0; i < actions.length; i++) {
			if(!actions[i].canUpdate(context))
				deltas[i] = 0;
			else if(deltas[i] == 0)
				deltas[i] = actions[i].getDelta(context);
			else {
				deltas[i] -= delta;
				if(deltas[i] <= 0) {
					UpdateAction action = actions[i];
					final int idx = i;
					action.update(context, () -> datas[idx], value -> datas[idx] = value);
					deltas[i] = 0;
				}
			}
			
			if(minWait == 0)
				minWait = deltas[i];
			else if(deltas[i] != 0)
				minWait = Math.min(minWait, deltas[i]);
		}
		
		return minWait;*/
	}
}
