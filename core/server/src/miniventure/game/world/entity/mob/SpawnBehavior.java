package miniventure.game.world.entity.mob;

import java.util.Arrays;
import java.util.EnumSet;

import miniventure.game.world.TimeOfDay;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.Nullable;

public interface SpawnBehavior {
	
	SpawnBehavior DEFAULT = custom(null);
	SpawnBehavior DEFAULT_NIGHT = custom(TimeOfDay.Night);
	
	static SpawnBehavior custom(@Nullable TimeOfDay spawnTime, TileTypeEnum... types) {
		final EnumSet<TileTypeEnum> tileTypes = types.length == 0 ? EnumSet.noneOf(TileTypeEnum.class) : EnumSet.copyOf(Arrays.asList(types));
		return new SpawnBehavior() {
			@Override
			public boolean maySpawn(Entity e) {
				return spawnTime == null ||
					TimeOfDay.getTimeOfDay(e.getWorld().getDaylightOffset()) == spawnTime;
			}
			
			@Override
			public boolean hasTiles() { return tileTypes.size() > 0; }
			
			@Override
			public boolean maySpawn(TileTypeEnum type) {
				return tileTypes.contains(type);
			}
		};
	}
	
	boolean maySpawn(Entity e);
	boolean maySpawn(TileTypeEnum type);
	boolean hasTiles();
}
