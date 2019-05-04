package miniventure.game.world.worldgen.island;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileDelegator implements TileProcessor {
	
	@NotNull private final TileCondition condition;
	@Nullable private final TileProcessor ifTrue;
	@Nullable private final TileProcessor ifFalse;
	
	public TileDelegator(@NotNull TileCondition condition, @Nullable TileProcessor ifTrue, @Nullable TileProcessor ifFalse) {
		this.condition = condition;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
	
	@Override
	public void processTile(ProtoTile tile) {
		TileProcessor conditionalProcess = condition.isMatch(tile) ? ifTrue : ifFalse;
		if(conditionalProcess != null)
			conditionalProcess.processTile(tile);
	}
}
