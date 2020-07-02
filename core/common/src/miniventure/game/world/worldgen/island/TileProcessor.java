package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TileProcessor {
	
	void processTile(ProtoTile tile);
	
	default TileProcessor append(TileProcessor other) {
		if(other instanceof TileMultiProcess)
			return other.append(this);
		return new TileMultiProcess(this, other);
	}
	
	static TileProcessor append(@NotNull TileProcessor first, TileProcessor... processors) {
		for(TileProcessor p: processors)
			first = first.append(p);
		return first;
	}
	
	default TileProcessor copy() { return this; }
	
	class TileMultiProcess implements TileProcessor {
		
		private final LinkedList<TileProcessor> processors;
		
		private TileMultiProcess(TileProcessor p1, TileProcessor p2) {
			this.processors = new LinkedList<>();
			processors.add(p1);
			processors.add(p2);
		}
		private TileMultiProcess(TileMultiProcess model) {
			processors = new LinkedList<>(model.processors);
		}
		
		@Override
		public TileProcessor append(TileProcessor other) {
			if(other instanceof TileMultiProcess)
				processors.addAll(((TileMultiProcess)other).processors);
			else if(other != null)
				processors.add(other);
			return this;
		}
		
		@Override
		public TileProcessor copy() {
			return new TileMultiProcess(this);
		}
		
		@Override
		public void processTile(ProtoTile tile) {
			for(TileProcessor processor: processors)
				processor.processTile(tile);
		}
	}
	
}
