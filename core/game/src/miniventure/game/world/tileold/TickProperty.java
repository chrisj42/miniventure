package miniventure.game.world.tileold;

import miniventure.game.util.function.VoidMonoFunction;

import org.jetbrains.annotations.NotNull;

public class TickProperty extends TileProperty {
	
	private final VoidMonoFunction<Tile> tick;
	
	TickProperty(@NotNull TileType tileType, VoidMonoFunction<Tile> tick) {
		super(tileType);
		this.tick = tick;
	}
	
	public void tick(Tile tile) { tick.act(tile); }
	
	/*
	public static class DelayedTickProperty extends TickProperty {
		
		private final int tickDelay;
		
		DelayedTickProperty(@NotNull TileType tileType, int tickDelay, VoidMonoFunction<Tile> tickAction) {
			super(tileType, tickAction);
			this.tickDelay = tickDelay;
		}
		
		@Override
		public void tick(Tile tile) {
			
		}
		
		@Override
		public String[] getInitialData() {
			return new String[] {"0"};
		}
	}*/
}
