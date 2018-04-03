package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class SpreadProperty {
	
	interface TileReplaceBehavior {
		void spreadType(TileType newType, Tile tile);
	}
	
	private final TileType tileType;
	private final TileReplaceBehavior replaceBehavior;
	private final HashSet<TileType> replaces;
	
	SpreadProperty(@NotNull TileType tileType, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		this.tileType = tileType;
		this.replaceBehavior = replaceBehavior;
		this.replaces = new HashSet<>(Arrays.asList(replaces));
	}
	
	
	public boolean canSpread(Tile tile) {
		if(tile.getType() != tileType) {
			System.err.println("Warning: SpreadProperty for " + tileType + " being used for tile " + tile + "; not updating");
			return false; // the current tile being updated is not of the original tile type which is supposed to be spreading. This should never happen, but it can't hurt anything to have this here.
		}
		
		for(Tile t: tile.getAdjacentTiles(false))
			if(replaces.contains(t.getType()))
				return true;
		
		return false;
	}
	
	public void spread(Tile tile) {
		Array<Tile> around = tile.getAdjacentTiles(false);
		//around.shuffle();
		for(Tile t: around) {
			if(replaces.contains(t.getType())) {
				replaceBehavior.spreadType(tileType, t);
				//break;
			}
		}
	}
	
}
