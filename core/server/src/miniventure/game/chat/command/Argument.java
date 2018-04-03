package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.server.ServerCore;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.entity.mob.ServerPlayer;

public interface Argument {
	
	boolean satisfiedBy(String[] args, int offset);
	int length();
	
	static Argument[] noArgs() { return new Argument[0]; }
	static Argument[] singleArg(ArgumentValidator validator) {
		return new Argument[] {new Argument() {
			@Override
			public boolean satisfiedBy(String[] args, int offset) {
				return validator.isValid(args[offset]);
			}
			@Override public int length() { return 1; }
		}};
	}
	
	static Argument varArg(ArgumentValidator validator) {
		return new Argument() {
			@Override
			public boolean satisfiedBy(String[] args, int offset) {
				return validator.isValid(String.join(" ", Arrays.copyOfRange(args, offset, args.length)));
			}
			
			@Override
			public int length() { return -1; }
		};
	}
	
	interface ArgumentValidator<T> {
		static <T> T notNull(ValueFunction<T> function) throws IllegalArgumentException {
			T obj = function.get();
			if(obj == null)
				throw new IllegalArgumentException();
			return obj;
		}
		static <T> T noException(ValueFunction<T> function) throws IllegalArgumentException {
			T obj;
			try {
				obj = function.get();
			} catch(Throwable t) {
				throw new IllegalArgumentException();
			}
			
			return obj;
		}
		static <T> T isTrue(ValueFunction<T> valueFunction, ValueMonoFunction<Boolean, T> boolFunction) throws IllegalArgumentException {
			T obj = valueFunction.get();
			if(!boolFunction.get(obj))
				throw new IllegalArgumentException();
			
			return obj;
		}
		
		ArgumentValidator<Integer> INTEGER = arg -> noException(() -> Integer.parseInt(arg));
		ArgumentValidator<Double> DECIMAL = arg -> noException(() -> Double.parseDouble(arg));
		ArgumentValidator<ServerPlayer> PLAYER = arg -> notNull(() -> ServerCore.getServer().getPlayerByName(arg));
		ArgumentValidator<Command> COMMAND = arg -> noException(() -> Enum.valueOf(Command.class, arg.toUpperCase()));
		
		static ArgumentValidator<String> exactString(String match, boolean matchCase) {
			return arg -> isTrue(() -> arg, theArg -> matchCase ? theArg.equals(match) : theArg.equalsIgnoreCase(match));
		}
		
		T get(String arg) throws IllegalArgumentException;
		
		default boolean isValid(String arg) {
			try {
				get(arg);
			} catch(IllegalArgumentException ex) {
				return false;
			}
			
			return true;
		}
	}
	
}
