package miniventure.game.world.worldgen.noise;

public interface NoiseMapFetcher {
	
	interface NoiseValueFetcher {
		float get(int x, int y);
	}
	
	NoiseValueFetcher get(GenInfo info);
	
	static NoiseMapFetcher get(NoiseValueFetcher fetcher) {
		return info -> fetcher;
	}
	
	static NoiseMapFetcher get(float constant) {
		return get((x, y) -> constant);
	}
}
