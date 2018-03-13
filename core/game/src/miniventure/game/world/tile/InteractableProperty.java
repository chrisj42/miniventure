package miniventure.game.world.tile;

import miniventure.game.item.type.Item;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InteractableProperty extends TileProperty {
	
	boolean interact(Player player, @Nullable Item heldItem, Tile tile);
	
	@Override
	default Class<? extends TileProperty> getUniquePropertyClass() { return InteractableProperty.class; }
}
