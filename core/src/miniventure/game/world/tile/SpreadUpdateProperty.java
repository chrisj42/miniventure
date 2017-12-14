package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import com.badlogic.gdx.utils.Array;

public class SpreadUpdateProperty implements UpdateProperty {
	
	private final HashSet<TileType> replaces;
	
	public SpreadUpdateProperty(TileType... replaces) {
		this.replaces = new HashSet<>(Arrays.asList(replaces));
	}
	
	@Override
	public void update(float delta, Tile tile) {
		Array<Tile> around = tile.getAdjacentTiles(false);
		for(Tile t: around) {
			if(replaces.contains(t.getType())) {
				t.resetTile(tile.getType());
				//break;
			}
		}
	}
}
