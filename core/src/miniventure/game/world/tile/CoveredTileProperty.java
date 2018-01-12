package miniventure.game.world.tile;

public class CoveredTileProperty implements TileProperty {
	
	private final TileType coveredTile;
	private TileType tileType;
	private int dataLength = 0;
	
	CoveredTileProperty(TileType coveredTile) {
		this.coveredTile = coveredTile;
	}
	
	@Override
	public void init(TileType type) {
		this.tileType = type;
		if(coveredTile == null && !type.isGroundTile())
			dataLength = 1;
	}
	
	// this is called when resetting a tile. 
	public void tilePlaced(Tile tile, TileType previous) {
		if(previous == null) return;
		if(dataLength > 0) // fetch from data
			tile.setData(this, tileType,0, previous.ordinal());
		else if(tile.getType() != tileType/* || previous != coveredTile*/)
			System.err.println("Warning: unexpected placement of tile type "+tile.getType()+", using property for " + tileType);
	}
	
	public TileType getCoveredTile() { return coveredTile; }
	public TileType getCoveredTile(Tile tile) {
		if(dataLength > 0) 
			return TileType.values[tile.getData(this, tileType,0)];
		return coveredTile;
	}
	
	//boolean hasUnderTileData() { return dataLength > 0; }
	
	@Override
	public Integer[] getInitData() { return new Integer[dataLength]; }
}
