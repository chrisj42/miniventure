package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import com.badlogic.gdx.utils.Array;

public class SpreadUpdateProperty implements UpdateProperty {
	
	private final HashSet<TileType> replaces;
	private TileType type;
	
	SpreadUpdateProperty(TileType... replaces) {
		this.replaces = new HashSet<>(Arrays.asList(replaces));
	}
	
	@Override
	public void init(TileType type) { this.type = type; }
	
	@Override
	public void update(float delta, Tile tile) {
		//boolean isGround = type.isGroundTile();
		if(!tile.hasType(type)) {
			System.err.println("Warning: SpreadUpdateProperty for " + type + " being used for tile " + tile + "; not updating");
			return; // the current tile being updated is not of the original tile type which is supposed to be spreading. This should never happen, but it can't hurt anything to have this here.
		}
		
		Array<Tile> around = tile.getAdjacentTiles(false);
		around.shuffle();
		for(Tile t: around) {
			if(replaces.contains(t.getType())) {
				t.addTile(type);
				//break;
			}
		}
	}
}
