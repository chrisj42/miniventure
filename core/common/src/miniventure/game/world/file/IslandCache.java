package miniventure.game.world.file;

import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.world.worldgen.island.IslandType;

public class IslandCache {
	
	// holds the LevelCache for the surface and cavern levels
	
	public interface LevelCacheFetcher {
		LevelCache getCache(IslandCache island, boolean isSurface);
	}
	
	public final IslandReference ref;
	
	public final LevelCache surface, caverns;
	
	// world gen
	IslandCache(int id, long seed, IslandType islandType) {
		this(id,
			(island, isSurface) -> new LevelCache(island, isSurface, seed, islandType),
			(island, isSurface) -> new LevelCache(island, isSurface, seed, islandType)
		);
	}
	
	IslandCache(int id, LevelCacheFetcher surface, LevelCacheFetcher caverns) {
		this.surface = surface.getCache(this, true);
		this.caverns = caverns.getCache(this, false);
		
		ref = new IslandReference(id, this.surface.islandType);
	}
	
	int getId(boolean isSurface) {
		return isSurface ? ref.levelId : -ref.levelId;
	}
}
