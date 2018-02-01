package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

public class CoveredTileProperty implements TileProperty {
	
	@Nullable private final HashSet<TileType> coveredTiles;
	//private TileType tileType;
	
	CoveredTileProperty(@Nullable TileType... coveredTiles) {
		// param is null, means any type can be under
		// otherwise, only specified types can be under this one.
		this.coveredTiles = coveredTiles == null ? null : new HashSet<>(Arrays.asList(coveredTiles));
	}
	
	//@Override
	//public void init(TileType type) { this.tileType = type; }
	
	// this is called when resetting a tile. 
	/*void tilePlaced(Tile tile, TileType previous) {
		if(previous == null) return;
		if(!singleCoveredTile) // fetch from data
			tile.setData(getClass(), tileType,0, previous.ordinal()+"");
		else if(tile.getType() != tileType*//* || previous != coveredTile*//*)
			System.err.println("Warning: unexpected placement of tile type "+tile.getType()+", using property for " + tileType);
	}*/
	
	boolean canCover(TileType type) {
		if(coveredTiles == null) return true;
		
		return coveredTiles.contains(type);
	}
	
	public TileType[] getCoverableTiles() { return coveredTiles == null ? null : coveredTiles.toArray(new TileType[coveredTiles.size()]); }
	
	/*@NotNull
	public TileType getCoveredTile(Tile tile) {
		if(!singleCoveredTile)
			return TileType.values[new Integer(tile.getData(getClass(), tileType,0))];
		else if(coveredTile == null)
			return tileType;
		else
			return coveredTile;
	}*/
}
