package miniventure.game.world.tile;

import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.world.entity.mob.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface InteractionManager extends TileProperty {
	
	Result onInteract(@NotNull ServerTile tile, Player player, @Nullable ServerItem item);
	
	@Override
	default void registerDataTypes(TileType tileType) {}
	
	InteractionManager NONE = (tile, player, item) -> Result.NONE;
}
