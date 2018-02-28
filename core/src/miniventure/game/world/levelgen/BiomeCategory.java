package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public enum BiomeCategory {
	
	WET(
		Biome.OCEAN+"_1"
	),
	
	DRY(
		Biome.DESERT+"_1",
		Biome.PLAINS+"_4",
		Biome.FOREST+"_2"
	),
	
	ROCKY(
		Biome.MOUNTAIN+"_1"
	);
	
	@NotNull
	private final EnumFetcher<Biome> biomeFetcher;
	
	BiomeCategory(@NotNull String... biomesWithOccurances) {
		biomeFetcher = new EnumFetcher<>(Biome.class, biomesWithOccurances);
	}
	
	public Biome getBiome(float noise) { return biomeFetcher.getType(noise); }
}
