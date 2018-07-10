package miniventure.game.world.levelgen.util;

@FunctionalInterface
public interface ValueListener<T> {
	void onValueSet(T value);
}
