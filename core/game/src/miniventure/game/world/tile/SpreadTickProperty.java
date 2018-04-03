package miniventure.game.world.tile;

import miniventure.game.world.tile.SpreadProperty.TileReplaceBehavior;

import org.jetbrains.annotations.NotNull;

public class SpreadTickProperty extends TickProperty {
	
	private final SpreadProperty spreadProperty;
	
	SpreadTickProperty(@NotNull TileType tileType, TileReplaceBehavior replaceBehavior, TileType... replaces) {
		super(tileType, null);
		spreadProperty = new SpreadProperty(tileType, replaceBehavior, replaces);
	}
	
	@Override
	public void tick(Tile tile) {
		if(spreadProperty.canSpread(tile))
			spreadProperty.spread(tile);
	}
}
