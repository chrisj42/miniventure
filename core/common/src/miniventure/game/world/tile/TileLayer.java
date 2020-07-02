package miniventure.game.world.tile;

public enum TileLayer {
	
	// connections occur only on same layer
	// overlap occurs on same or lower layer
	
	GroundLayer, DecLayer, ObjectLayer;
	
	public static final TileLayer[] values = TileLayer.values();
}
