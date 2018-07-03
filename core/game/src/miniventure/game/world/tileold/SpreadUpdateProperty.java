package miniventure.game.world.tileold;

import miniventure.game.world.tile.SpreadProperty.TileReplaceBehavior;
import miniventure.game.world.tile.UpdateProperty.DelayedUpdate;

import org.jetbrains.annotations.NotNull;

public class SpreadUpdateProperty extends DelayedUpdate {
	
	private SpreadProperty spreadProperty;
	
	SpreadUpdateProperty(@NotNull TileType tileType, float spreadDelay, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		super(tileType, spreadDelay, null, null); // will specify them directly
		spreadProperty = new SpreadProperty(tileType, replaceBehavior, replaces);
	}
	
	@Override
	public boolean shouldUpdate(Tile tile) { return spreadProperty.canSpread(tile); }
	
	@Override
	public void update(Tile tile) { spreadProperty.spread(tile); }
}
