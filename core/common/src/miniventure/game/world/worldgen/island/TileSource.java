package miniventure.game.world.worldgen.island;

import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TileSource {
	
	TileTypeEnum getTileType(float value);
	
	class TypeSource implements TileSource {
		
		@NotNull
		private final TileTypeEnum type;
		
		public TypeSource(@NotNull TileTypeEnum type) {
			this.type = type;
		}
		
		@Override @NotNull
		public TileTypeEnum getTileType(float value) {
			return type;
		}
	}
	
}
