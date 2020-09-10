package miniventure.game.world.tile;

/** @noinspection rawtypes*/
public class TileTypeDataOrder extends SerialEnumOrderedDataSet<TileDataTag> {
	
	private final TileType tileType;
	
	TileTypeDataOrder(TileType tileType) {
		super(TileDataTag.class);
		this.tileType = tileType;
	}
	
	public TileType getTileType() { return tileType; }
}
