package miniventure.game.world.tile;

import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class SolidProperty extends TileProperty {
	
	public static SolidProperty get(@NotNull TileType tileType, boolean solid) { return new SolidProperty(tileType, e -> !solid); }
	
	private final ValueMonoFunction<Entity, Boolean> permeable;
	
	SolidProperty(@NotNull TileType tileType, ValueMonoFunction<Entity, Boolean> permeable) {
		super(tileType);
		
		this.permeable = permeable;
	}
	
	public boolean isPermeableBy(Entity e) { return permeable.get(e); }
	
}
