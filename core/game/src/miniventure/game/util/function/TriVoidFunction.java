package miniventure.game.util.function;

@FunctionalInterface
public interface TriVoidFunction<P1, P2, P3> {
	void act(P1 obj1, P2 obj2, P3 obj3);
}
