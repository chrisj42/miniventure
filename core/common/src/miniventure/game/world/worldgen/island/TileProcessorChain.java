package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// iterates over the given list of conditions and stops at the first one that returns true.
// each condition will execute 
public class TileProcessorChain implements TileProcessor {
	
	public static class ProcessChainBuilder {
		
		private LinkedList<TileCondition> conditions;
		private LinkedList<TileProcessor> processors;
		
		public ProcessChainBuilder() {
			conditions = new LinkedList<>();
			processors = new LinkedList<>();
		}
		
		public ProcessChainBuilder add(@NotNull TileCondition condition, @Nullable TileProcessor processor) {
			conditions.add(condition);
			processors.add(processor);
			return this;
		}
		
		public TileProcessorChain getChain() {
			TileCondition[] conditions = this.conditions.toArray(new TileCondition[0]);
			TileProcessor[] processors = this.processors.toArray(new TileProcessor[0]);
			return new TileProcessorChain(conditions, processors);
		}
	}
	
	private final TileCondition[] conditions;
	private final TileProcessor[] processors;
	
	private TileProcessorChain(TileCondition[] conditions, TileProcessor[] processors) {
		this.conditions = conditions;
		this.processors = processors;
	}
	
	@Override
	public void processTile(ProtoTile tile) {
		for(int i = 0; i < conditions.length; i++) {
			if(conditions[i].isMatch(tile)) {
				if(processors[i] != null)
					processors[i].processTile(tile);
				break;
			}
		}
	}
}
