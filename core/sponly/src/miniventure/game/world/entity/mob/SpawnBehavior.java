package miniventure.game.world.entity.mob;

import java.util.Arrays;
import java.util.EnumSet;

import miniventure.game.world.Point;
import miniventure.game.world.management.Level;
import miniventure.game.world.management.TimeOfDay;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SpawnBehavior {
	
	int DEFAULT_MAX_SPAWN_ATTEMPTS = Level.ACTIVE_RADIUS * Level.ACTIVE_RADIUS * 2;
	
	SpawnBehavior DEFAULT = new SimpleSpawnBehavior(null);
	SpawnBehavior DEFAULT_NIGHT = new SimpleSpawnBehavior(TimeOfDay.Night);
	
	boolean maySpawn(@NotNull Level level);
	boolean maySpawn(TileType type);
	
	@Nullable
	Tile trySpawn(@NotNull Level level);
	
	class SimpleSpawnBehavior implements SpawnBehavior {
		
		private static final EnumSet<TileType> DEFAULT_SPAWN_TILES =
				EnumSet.of(TileType.GRASS, TileType.DIRT, TileType.SAND, TileType.SNOW);
		
		private final TimeOfDay spawnTime;
		private final EnumSet<TileType> tileTypes;
		
		SimpleSpawnBehavior(@Nullable TimeOfDay spawnTime, TileType... tileTypes) {
			this.spawnTime = spawnTime;
			if(tileTypes.length == 0)
				this.tileTypes = DEFAULT_SPAWN_TILES;
			else
				this.tileTypes = EnumSet.copyOf(Arrays.asList(tileTypes));
		}
		
		@Override
		public boolean maySpawn(@NotNull Level level) {
			if(spawnTime == null) return true;
			
			TimeOfDay curTime = TimeOfDay.getTimeOfDay(level.getWorld().getDaylightOffset());
			return curTime == spawnTime;
		}
		
		@Override
		public boolean maySpawn(TileType type) {
			return tileTypes.contains(type);
		}
		
		@Override @Nullable
		public Tile trySpawn(@NotNull Level level) {
			if(!maySpawn(level)) return null;
			
			// spawn only within a radius of the player
			final Point spawnCenter = level.getPlayer().getClosestTile().getLocation();
			Point minPos = new Point(
					Math.max(0, spawnCenter.x - Level.ACTIVE_RADIUS),
					Math.max(0, spawnCenter.y - Level.ACTIVE_RADIUS)
			);
			Point maxPos = new Point(
					Math.min(level.getWidth()-1, spawnCenter.x + Level.ACTIVE_RADIUS),
					Math.min(level.getHeight()-1, spawnCenter.y + Level.ACTIVE_RADIUS)
			);
			for(int i = 0; i < DEFAULT_MAX_SPAWN_ATTEMPTS; i++) {
				final int x = MathUtils.random(minPos.x, maxPos.x);
				final int y = MathUtils.random(minPos.y, maxPos.y);
				Tile tile = level.getTile(x, y);
				if(maySpawn(tile.getType()))
					return tile;
			}
			
			// searched through enough tiles; expected to be easy to find spawnable locations, so if we can't find something then there's probably an issue. In the case that they're really unlucky, they can try again.
			return null;
		}
	}
}
