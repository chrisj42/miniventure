package miniventure.game.util.function;

@FunctionalInterface
public interface ValueBiFunction<RT, P1, P2> {
	RT get(P1 obj1, P2 obj2);
}
