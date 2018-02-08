package miniventure.game.world.levelgen;

import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

public class BiomeSelector {
	
	@NotNull private final EnumFetcher<Biome> biomeFetcher;
	
	public BiomeSelector(@NotNull String... biomesWithOccurrences) {
		biomeFetcher = new EnumFetcher<>(Biome.class, biomesWithOccurrences);
	}
	
	Biome getBiome(float noise) { return biomeFetcher.getType(noise); }
	
	public TileType getTile(float noise) { return biomeFetcher.getType(noise).getTile(noise); }
	
}
