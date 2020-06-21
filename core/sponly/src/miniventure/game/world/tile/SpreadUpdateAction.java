package miniventure.game.world.tile;

import java.util.EnumSet;

import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.Tile.TileContext;
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
	
	private final FloatFetcher spreadDelayFetcher;
	private final float spreadChance;
	private final TileReplaceBehavior replaceBehavior;
	private final EnumSet<TileType> replaces;
	
	SpreadUpdateAction(float spreadDelay, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		this(FloatFetcher.value(spreadDelay), replaceBehavior, replaces);
	}
	SpreadUpdateAction(FloatFetcher spreadDelayFetcher, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		this(spreadDelayFetcher, 1, replaceBehavior, replaces);
	}
	SpreadUpdateAction(FloatFetcher spreadDelayFetcher, float spreadChance, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		this.spreadDelayFetcher = spreadDelayFetcher;
		this.spreadChance = spreadChance;
		this.replaceBehavior = replaceBehavior;
		this.replaces = MyUtils.enumSet(replaces);
	}
	
	@Override
	public float update(TileContext context) {
		if(!canSpread(context.getTile()))
			return 0; // no need to check for spreadability anymore until adjacent tiles are updated
		
		if(MathUtils.random() < spreadChance) { // must be less to execute; a chance of 1 will always execute.
			for (Tile t: context.getTile().getAdjacentTiles(false)) {
				if (replaces.contains(t.getType())) {
					replaceBehavior.spreadType(context.getType(),  t);
					//break;
				}
			}
		}
		
		return spreadDelayFetcher.getFloat();
	}
	
	private boolean canSpread(@NotNull Tile tile) {
		for(Tile t: tile.getAdjacentTiles(false))
			if(replaces.contains(t.getType()))
				return true;
		
		return false;
	}
}
