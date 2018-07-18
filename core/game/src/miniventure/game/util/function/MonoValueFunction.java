package miniventure.game.util.function;

@FunctionalInterface
public interface MonoValueFunction<PT, RT> {
	RT get(PT val);
}
