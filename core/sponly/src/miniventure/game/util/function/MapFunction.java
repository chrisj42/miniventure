package miniventure.game.util.function;

@FunctionalInterface
public interface MapFunction<PT, RT> {
	RT get(PT obj);
}
