package miniventure.game.util.function;

@FunctionalInterface
public interface ValueFunction<RT> {
	RT get();
}
