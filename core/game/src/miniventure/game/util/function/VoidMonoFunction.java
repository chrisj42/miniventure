package miniventure.game.util.function;

@FunctionalInterface
public interface VoidMonoFunction<PT> {
	void act(PT obj);
}
