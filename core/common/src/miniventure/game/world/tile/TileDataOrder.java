package miniventure.game.world.tile;

/** @noinspection rawtypes*/
public class TileDataOrder extends SerialEnumOrderedDataSet<TileDataTag> {
	
	private final TileType tileType;
	
	TileDataOrder(TileType tileType) {
		super(TileDataTag.class);
		this.tileType = tileType;
	}
	
	public TileType getTileType() { return tileType; }
}
