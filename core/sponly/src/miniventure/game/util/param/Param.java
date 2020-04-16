package miniventure.game.util.param;

/**
 * The goal of this class is to provide an easy, extendable way to make a
 * quick set of named generic instances.
 * 
 * 
 * 
 * @param <T> parameter type
 */
public class Param<T> {
	
	private final T defaultValue;
	
	public Param(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Value<T> as(T val) { return new Value<>(this, val); }
	
	public T getDefault() { return defaultValue; }
}
