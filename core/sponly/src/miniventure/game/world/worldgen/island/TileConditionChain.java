package miniventure.game.world.worldgen.island;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// iterates over the given list of conditions and stops at the first one that returns true. 
public class TileConditionChain implements TileProcessor {
	
	public static ConditionChainBuilder builder() {
		return new ConditionChainBuilder();
	}
	
	public static class ConditionChainBuilder {
		
		private LinkedList<TileCondition> conditions;
		private LinkedList<TileProcessor> processors;
		
		private ConditionChainBuilder() {
			conditions = new LinkedList<>();
			processors = new LinkedList<>();
		}
		
		public ConditionChainBuilder add(@NotNull TileCondition condition, @Nullable TileProcessor processor) {
			conditions.add(condition);
			processors.add(processor);
			return this;
		}
		
		public TileConditionChain getChain() { return getChain(null); }
		public TileConditionChain getChain(@Nullable TileProcessor onAllFail) {
			TileCondition[] conditions = this.conditions.toArray(new TileCondition[0]);
			TileProcessor[] processors = this.processors.toArray(new TileProcessor[0]);
			return new TileConditionChain(conditions, processors, onAllFail);
		}
	}
	
	private final TileCondition[] conditions;
	private final TileProcessor[] processors;
	private final TileProcessor elseProcessor;
	
	private TileConditionChain(TileCondition[] conditions, TileProcessor[] processors, @Nullable TileProcessor elseProcessor) {
		this.conditions = conditions;
		this.processors = processors;
		this.elseProcessor = elseProcessor;
	}
	
	@Override
	public void processTile(ProtoTile tile) {
		boolean processed = false;
		for(int i = 0; i < conditions.length; i++) {
			if(conditions[i].isMatch(tile)) {
				if(processors[i] != null)
					processors[i].processTile(tile);
				processed = true;
				break;
			}
		}
		if(!processed && elseProcessor != null)
			elseProcessor.processTile(tile);
	}
}
