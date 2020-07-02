package miniventure.game.world.file;

import java.io.IOException;

import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.worldgen.level.IslandType;

public class IslandCache {
	
	// holds the LevelCache for the surface and cavern levels
	
	public interface LevelCacheFetcher {
		LevelCache getCache(IslandCache island, int id) throws IOException;
	}
	
	private final IslandReference ref;
	private final long seed;
	private final LevelCache[] levels;
	
	// world gen
	IslandCache(int id, long seed, IslandType islandType) {
		this.seed = seed;
		ref = new IslandReference(id, islandType);
		levels = new LevelCache[islandType.getLevelCount()];
		for(int i = 0; i < levels.length; i++)
			levels[i] = new LevelCache(this, i, seed + i);
	}
	
	// load
	IslandCache(int id, long seed, IslandType islandType, LevelCacheFetcher cacheFetcher) throws IOException {
		ref = new IslandReference(id, islandType);
		this.seed = seed;
		levels = new LevelCache[islandType.getLevelCount()];
		for (int i = 0; i < levels.length; i++)
			levels[i] = cacheFetcher.getCache(this, i);
	}
	
	// game will do a hasDepth() check to determine if digging through the ground should allow you to go a level below.
	public int getLevelCount() {
		return levels.length;
	}
	
	public LevelCache getCache(int depth) {
		return /*depth >= levels.length ? null : */levels[depth];
	}
	
	public int getId() {
		return ref.islandId;
	}
	
	public IslandType getIslandType() {
		return ref.type;
	}
	
	public IslandReference getRef() {
		return ref;
	}
}
