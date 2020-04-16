package miniventure.game.util.function;

@FunctionalInterface
public interface ValueAction<PT> {
	void act(PT obj);
}
