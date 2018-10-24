package miniventure.game.util.function;

@FunctionalInterface
public interface ValueFunction<PT> {
	void act(PT obj);
}
