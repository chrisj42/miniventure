package miniventure.game.util.function;

@FunctionalInterface
public interface MonoVoidFunction<PT> {
	void act(PT obj);
}
