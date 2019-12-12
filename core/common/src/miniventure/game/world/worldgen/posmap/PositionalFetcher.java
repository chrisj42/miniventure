package miniventure.game.world.worldgen.posmap;

@FunctionalInterface
public interface PositionalFetcher<T> {
	T get(int x, int y);
	
	@FunctionalInterface
	interface PositionalCheck extends PositionalFetcher<Boolean> {}
}
