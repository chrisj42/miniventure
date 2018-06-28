package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public enum BiomeCategory {
	
	WET(
		Biome.OCEAN+"_1"
	),
	
	MID(
		Biome.PLAINS+"_7",
		Biome.OCEAN+"_1",
		Biome.FOREST+"_3"
	),
	
	DRY(
		Biome.DESERT+"_2",
		Biome.PLAINS+"_4",
		Biome.FOREST+"_1"
	),
	
	ROCKY(
		Biome.MOUNTAIN+"_1",
		Biome.CANYON+"_2",
		Biome.MOUNTAIN+"_1"
	);
	
	@NotNull
	private final EnumFetcher<Biome> biomeFetcher;
	
	BiomeCategory(@NotNull String... biomesWithOccurances) {
		biomeFetcher = new EnumFetcher<>(Biome.class, biomesWithOccurances);
	}
	
	public Biome getBiome(float noise) { return biomeFetcher.getType(noise); }
}
