package miniventure.game.world.worldgen.island;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TileCondition {
	
	boolean isMatch(ProtoTile tile);
	
	default TileProcessor onMatch(@NotNull TileProcessor doOnMatch) {
		return tile -> {
			if(isMatch(tile))
				doOnMatch.processTile(tile);
		};
	}
	
	default TileProcessor onMatchElse(@Nullable TileProcessor match, @Nullable TileProcessor noMatch) {
		return tile -> {
			TileProcessor p = isMatch(tile) ? match : noMatch;
			if(p != null)
				p.processTile(tile);
		};
	}
	
}
