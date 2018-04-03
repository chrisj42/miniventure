package miniventure.game.util.function;

@FunctionalInterface
public interface ValueTriFunction<RT, P1, P2, P3> {
	RT get(P1 obj1, P2 obj2, P3 obj3);
}
