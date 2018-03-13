package miniventure.game.util.function;

@FunctionalInterface
public interface ValueMonoFunction<RT, PT> {
	RT get(PT val);
}
