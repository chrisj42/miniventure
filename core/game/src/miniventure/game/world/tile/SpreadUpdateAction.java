package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.HashSet;

import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.UpdateManager.UpdateAction;

import org.jetbrains.annotations.NotNull;

class SpreadUpdateAction implements UpdateAction {
	
	@FunctionalInterface
	interface TileReplaceBehavior {
		void spreadType(TileType newType, Tile tile);
	}
	
	private final TileTypeEnum tileType;
	private final TileReplaceBehavior replaceBehavior;
	private final HashSet<TileTypeEnum> replaces;
	
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this.tileType = tileType;
		this.replaceBehavior = replaceBehavior;
		this.replaces = new HashSet<>(Arrays.asList(replaces));
	}
	
	@Override
	public float update(@NotNull Tile tile, float delta) {
		return 0;
	}
	
	public boolean canSpread(Tile tile) {
		for(Tile t: tile.getAdjacentTiles(false))
			if(replaces.contains(t.getType().getEnumType()))
				return true;
		
		return false;
	}
	
	public void spread(Tile tile) {
		HashSet<Tile> around = tile.getAdjacentTiles(false);
		//around.shuffle();
		for(Tile t: around) {
			if(replaces.contains(t.getType().getEnumType())) {
				replaceBehavior.spreadType(tileType.getTileType(t.getWorld()), t);
				//break;
			}
		}
	}
	
}
