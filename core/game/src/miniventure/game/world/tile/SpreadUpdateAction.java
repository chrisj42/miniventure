package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.VoidMonoFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.UpdateManager.UpdateAction;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

class SpreadUpdateAction implements UpdateAction {
	
	@FunctionalInterface
	interface TileReplaceBehavior {
		void spreadType(TileType newType, Tile tile);
	}
	
	private final TileTypeEnum tileType;
	private final ValueFunction<Float> spreadDelayFetcher;
	private final TileReplaceBehavior replaceBehavior;
	private final EnumSet<TileTypeEnum> replaces;
	
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, float spreadDelay, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this(tileType, () -> spreadDelay, replaceBehavior, replaces);
	}
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, float spreadDelayMin, float spreadDelayMax, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this(tileType, () -> MathUtils.random(spreadDelayMin, spreadDelayMax), replaceBehavior, replaces);
	}
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, ValueFunction<Float> spreadDelayFetcher, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this.tileType = tileType;
		this.spreadDelayFetcher = spreadDelayFetcher;
		this.replaceBehavior = replaceBehavior;
		this.replaces = EnumSet.copyOf(Arrays.asList(replaces));
	}
	
	@Override
	public void update(@NotNull Tile tile, ValueFunction<String> dataCacheFetcher, VoidMonoFunction<String> dataCacheSetter) {
		HashSet<Tile> around = tile.getAdjacentTiles(false);
		//around.shuffle();
		for(Tile t: around) {
			if(replaces.contains(t.getType().getEnumType())) {
				replaceBehavior.spreadType(tileType.getTileType(t.getWorld()), t);
				//break;
			}
		}
	}
	
	@Override
	public boolean canUpdate(@NotNull Tile tile) {
		for(Tile t: tile.getAdjacentTiles(false))
			if(replaces.contains(t.getType().getEnumType()))
				return true;
		
		return false;
	}
	
	@Override
	public float getDelta(@NotNull Tile tile) {
		return spreadDelayFetcher.get();
	}
	
}
