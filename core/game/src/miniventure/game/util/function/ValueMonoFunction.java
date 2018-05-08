package miniventure.game.util.function;

@FunctionalInterface
public interface ValueMonoFunction<PT, RT> {
	RT get(PT val);
}
