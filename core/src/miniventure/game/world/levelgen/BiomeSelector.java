package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public class BiomeSelector {
	
	@NotNull private final EnumFetcher<BiomeCategory> biomeFetcher;
	
	public BiomeSelector(@NotNull String... biomesWithOccurrences) {
		biomeFetcher = new EnumFetcher<>(BiomeCategory.class, biomesWithOccurrences);
	}
	
	BiomeCategory getBiome(float noise) { return biomeFetcher.getType(noise); }
	
	//public TileType getTile(float noise) { return biomeFetcher.getType(noise).getBiome(noise).getTile(noise); }
	
}
