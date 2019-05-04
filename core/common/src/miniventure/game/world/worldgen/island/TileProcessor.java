package miniventure.game.world.worldgen.island;

@FunctionalInterface
public interface TileProcessor {
	
	void processTile(ProtoTile tile);
	
	class TileMultiProcess implements TileProcessor {
		
		private final TileProcessor[] processors;
		
		public TileMultiProcess(TileProcessor... processors) {
			this.processors = processors;
		}
		
		@Override
		public void processTile(ProtoTile tile) {
			for(TileProcessor processor: processors)
				processor.processTile(tile);
		}
	}
	
}
