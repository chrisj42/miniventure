package miniventure.game.world.tile;

import miniventure.game.util.function.ValueTriFunction;
import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class TouchListener extends TileProperty {
	
	//TouchListener DO_NOTHING = entity -> {};
	
	private final ValueTriFunction<Entity, Tile, Boolean, Boolean> onTouch;
	
	TouchListener(@NotNull TileType tileType, ValueTriFunction<Entity, Tile, Boolean, Boolean> onTouch) {
		super(tileType);
		this.onTouch = onTouch;
	}
	
	public boolean touchedBy(Entity entity, Tile tile, boolean initial) { return onTouch.get(entity, tile, initial); }
	//void stillTouchedBy(Entity entity);
	//void steppedOff(Entity entity); // idk, this will be much later.
	
}
