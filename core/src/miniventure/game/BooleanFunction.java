package miniventure.game;

@FunctionalInterface
public interface BooleanFunction {
	boolean isTrue();
	
	BooleanFunction TRUE = () -> true; 
	BooleanFunction FALSE = () -> false; 
}
