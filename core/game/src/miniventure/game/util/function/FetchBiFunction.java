package miniventure.game.util.function;

@FunctionalInterface
public interface FetchBiFunction<P1, P2, RT> {
	RT get(P1 obj1, P2 obj2);
}
