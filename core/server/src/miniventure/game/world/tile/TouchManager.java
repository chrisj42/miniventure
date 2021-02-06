package miniventure.game.world.tile;

import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TouchManager extends TileProperty {
	
	void onTouched(@NotNull ServerTile tile, Entity entity, boolean initial);
	
	@Override
	default void registerDataTypes(TileType tileType) {}
	
	TouchManager NONE = (tile, entity, initial) -> {};
	
	static TouchManager DAMAGE_ENTITY(int damage) {
		return (tile, entity, initial) -> entity.attackedBy(tile, null, damage);
	}
}
