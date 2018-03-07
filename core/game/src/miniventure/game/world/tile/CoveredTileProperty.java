package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

public class CoveredTileProperty implements TileProperty {
	
	@Nullable private final HashSet<TileType> coveredTiles;
	
	CoveredTileProperty(@Nullable TileType... coveredTiles) {
		// param is null, means any type can be under
		// otherwise, only specified types can be under this one.
		this.coveredTiles = coveredTiles == null ? null : new HashSet<>(Arrays.asList(coveredTiles));
	}
	
	boolean canCover(TileType type) {
		if(coveredTiles == null) return true;
		
		return coveredTiles.contains(type);
	}
	
	public TileType[] getCoverableTiles() { return coveredTiles == null ? null : coveredTiles.toArray(new TileType[coveredTiles.size()]); }
	
	@Override
	public Class<? extends TileProperty> getUniquePropertyClass() { return CoveredTileProperty.class; }
}
