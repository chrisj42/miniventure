package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class SpreadUpdateProperty implements UpdateProperty {
	
	private final TileReplaceBehavior replaceBehavior;
	private final HashSet<TileType> replaces;
	private final TileType tileType;
	
	interface TileReplaceBehavior {
		void spreadType(TileType newType, Tile tile);
	}
	
	SpreadUpdateProperty(@NotNull TileType tileType, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		this.tileType = tileType;
		this.replaceBehavior = replaceBehavior;
		this.replaces = new HashSet<>(Arrays.asList(replaces));
	}
	
	@Override
	public void update(float delta, Tile tile) {
		if(!tile.hasType(tileType)) {
			System.err.println("Warning: SpreadUpdateProperty for " + tileType + " being used for tile " + tile + "; not updating");
			return; // the current tile being updated is not of the original tile type which is supposed to be spreading. This should never happen, but it can't hurt anything to have this here.
		}
		
		Array<Tile> around = tile.getAdjacentTiles(false);
		around.shuffle();
		for(Tile t: around) {
			if(replaces.contains(t.getType())) {
				replaceBehavior.spreadType(tileType, t);
				//break;
			}
		}
	}
}
