package miniventure.game.world.worldgen.noise;

public interface ValueSetFetcher {
	
	interface ValueFetcher {
		float get(int x, int y);
	}
	
	ValueFetcher get(long seed, int width, int height);
	
	static ValueSetFetcher get(ValueFetcher fetcher) {
		return (seed, width, height) -> fetcher;
	}
	
	static ValueSetFetcher get(float constant) {
		return get((x, y) -> constant);
	}
}
