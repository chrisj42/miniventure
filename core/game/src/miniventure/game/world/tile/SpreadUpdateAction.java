package miniventure.game.world.tile;

import java.util.EnumSet;
import java.util.HashSet;

import miniventure.game.util.MyUtils;
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
	
	@FunctionalInterface
	interface FloatFetcher {
		float getFloat();
		
		static FloatFetcher value(float val) { return () -> val; }
		static FloatFetcher random(float min, float max) { return () -> MathUtils.random(min, max); }
		static FloatFetcher random(float min, float avg, float max) {
			FloatFetcher one = random(min, avg);
			FloatFetcher two = random(avg, max);
			return () -> MathUtils.random(one.getFloat(), two.getFloat());
		}
	}
	
	private final TileTypeEnum tileType;
	private final FloatFetcher spreadDelayFetcher;
	private final float spreadChance;
	private final TileReplaceBehavior replaceBehavior;
	private final EnumSet<TileTypeEnum> replaces;
	
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, float spreadDelay, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this(tileType, FloatFetcher.value(spreadDelay), replaceBehavior, replaces);
	}
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, FloatFetcher spreadDelayFetcher, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this(tileType, spreadDelayFetcher, 1, replaceBehavior, replaces);
	}
	SpreadUpdateAction(@NotNull TileTypeEnum tileType, FloatFetcher spreadDelayFetcher, float spreadChance, TileReplaceBehavior replaceBehavior, TileTypeEnum... replaces) {
		this.tileType = tileType;
		this.spreadDelayFetcher = spreadDelayFetcher;
		this.spreadChance = spreadChance;
		this.replaceBehavior = replaceBehavior;
		this.replaces = MyUtils.enumSet(replaces);
	}
	
	@Override
	public void update(@NotNull Tile tile, ValueFunction<String> dataCacheFetcher, VoidMonoFunction<String> dataCacheSetter) {
		if(MathUtils.random() >= spreadChance) return; // must be less to execute; a chance of 1 will always execute.
		
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
	
	// not called repeatedly, unless updatable state changes repeatedly.
	@Override
	public float getDelta(@NotNull Tile tile) {
		return spreadDelayFetcher.getFloat();
	}
	
}
