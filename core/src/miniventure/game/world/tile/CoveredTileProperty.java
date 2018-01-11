package miniventure.game.world.tile;

public class CoveredTileProperty implements TileProperty {
	
	private final TileType underTile;
	private int dataLength = 0;
	
	CoveredTileProperty(TileType underTile) {
		this.underTile = underTile;
	}
	
	@Override
	public void init(TileType type) {
		if(underTile == null && type.getProp(AnimationProperty.class).isTransparent())
			dataLength = 1;
	}
	
	// this is called when resetting a tile. 
	public void tilePlaced(Tile tile, TileType previous) {
		if(underTile == null) {
			// it varies, fetch from data
			tile.setData(this, 0, previous.ordinal());
		}
		else if(previous != underTile && previous != null)
			System.err.println("Warning: under tile does not equal previous tile");
	}
	
	public TileType getUnderTile() { return underTile; }
	public TileType getUnderTile(Tile tile) {
		if(dataLength > 0) 
			return TileType.values[tile.getData(this, 0)];
		return underTile;
	}
	
	boolean hasUnderTileData() { return dataLength > 0; }
	
	@Override
	public Integer[] getInitData() { return new Integer[dataLength]; }
}
