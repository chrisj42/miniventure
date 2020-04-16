package miniventure.game.world.worldgen.noise;

@FunctionalInterface
public interface NoiseMapFetcher {
	
	@FunctionalInterface
	interface NoiseValueFetcher {
		float get(int x, int y);
	}
	
	NoiseValueFetcher preFetch(GenInfo info);
	
	static NoiseMapFetcher fetchFrom(NoiseValueFetcher fetcher) {
		return info -> fetcher;
	}
	
	static NoiseMapFetcher fetchAs(float constant) {
		return fetchFrom((x, y) -> constant);
	}
}
