package miniventure.game.world.tile;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.util.function.VoidMonoFunction;

import org.jetbrains.annotations.NotNull;

public abstract class UpdateProperty extends TileProperty {
	
	/*
		This interface specifies methods that are called to update tiles, and check if there is any state that should be dealt with.
		Because there are so many tiles, updating each one every frame just isn't an option, and random updates don't work for many things.
		So, to address the problem, we have this method. This is called whenever the tile (or an adjacent tile) is changed, which for now means that the top TileType is changed.
		This will allow for, say, water to flow to an adjacent hole tile when said hole comes into existence.
		
		Now, something that has to be accounted for is the fact that water shouldn't just immediately flow through a long line of holes, one hole per frame. It should move in intervals, perhaps every third of a second.
		To do this, we need a way to keep it updating. So that's just what we'll do. We will allow the update method to return a boolean for if the update should continue; if so, it won't be removed from the update queue. Though, we'll have to have a separate set of tiles that are continuing their update, rather than just starting, so that we can call the appropriate method. Both return a boolean for continued updating.
		
		Alright, so, here are the use cases for an update property:
			- water/fluid spreading. the first update will reset the duration counter, and check if it can flow into an adjacent space. such will be returned. The continued update method will add the delta, and return true as long as it is under a max duration. Upon reaching that duration, it will check for any tiles it can flow into, and flow into any found. It then returns false. Could reset the counter too, I suppose.
		
		This calls for a subclass that tracks the duration. it is passed a duration, an action condition, and an action. The action condition is checked in the first update, and if false, doesn't continue updating. Else, it starts counting, and doesn't check the condition again until the time is done (since it could be expensive). It then checks the condition, after the duration, and if true, executes the action passing in the tile.
	*/
	
	public static UpdateProperty noAct(@NotNull TileType tileType) {
		return new UpdateProperty(tileType) {
			@Override
			public boolean firstUpdate(Tile tile) { return false; }
			
			@Override
			public boolean update(float delta, Tile tile) { return false; }
		};
	}
	
	UpdateProperty(@NotNull TileType tileType) {
		super(tileType);
	}
	
	// returns whether the tile should continue to be updated every cycle.
	public abstract boolean firstUpdate(Tile tile);
	
	// returns same as first method. delta is the time since last cycle, as usual.
	public abstract boolean update(float delta, Tile tile);
	
	
	public static class DelayedUpdate extends UpdateProperty {
		
		private final float delay;
		private final ValueMonoFunction<Tile, Boolean> actionCondition;
		private final VoidMonoFunction<Tile> action;
		
		public DelayedUpdate(@NotNull TileType tileType, float delay, ValueMonoFunction<Tile, Boolean> actionCondition, VoidMonoFunction<Tile> action) {
			super(tileType);
			this.delay = delay;
			this.actionCondition = actionCondition;
			this.action = action;
		}
		
		protected boolean shouldUpdate(Tile tile) { return actionCondition.get(tile); }
		protected void update(Tile tile) { action.act(tile); }
		
		@Override
		public boolean firstUpdate(Tile tile) {
			boolean shouldUpdate = shouldUpdate(tile);
			if(shouldUpdate)
				tile.setData(TilePropertyType.Update, tileType, 0, "0");
			
			return shouldUpdate;
		}
		
		@Override
		public boolean update(float delta, Tile tile) {
			float timeElapsed = Float.parseFloat(tile.getData(TilePropertyType.Update, tileType, 0));
			timeElapsed += delta;
			if(timeElapsed >= delay) {
				if(shouldUpdate(tile))
					update(tile);
				return false;
			}
			
			tile.setData(TilePropertyType.Update, tileType, 0, timeElapsed+"");
			return true;
		}
		
		@Override
		public String[] getInitialData() {
			return new String[] {"0"};
		}
	}
}
