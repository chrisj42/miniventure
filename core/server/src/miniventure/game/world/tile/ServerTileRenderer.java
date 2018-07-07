package miniventure.game.world.tile;

public class ServerTileRenderer extends TileTypeRenderer {
	
	public ServerTileRenderer(TileTypeRenderer model) {
		super(model, null, null, new ServerTransitionManager(model.transitionManager));
	}
	
}
