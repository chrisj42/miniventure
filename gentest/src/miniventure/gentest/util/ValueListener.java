package miniventure.gentest.util;

@FunctionalInterface
public interface ValueListener<T> {
	void onValueSet(T value);
}
