package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.entity.mob.ServerPlayer;

import org.jetbrains.annotations.NotNull;

public interface Argument {
	
	boolean satisfiedBy(String[] args, int offset);
	int length();
	
	static Argument get(@NotNull ArgumentValidator... validators) {
		return new Argument() {
			@Override
			public boolean satisfiedBy(String[] args, int offset) {
				for(int i = 0; i < validators.length; i++)
					if(!validators[i].isValid(args[i]))
						return false;
				return true;
			}
			
			@Override
			public int length() { return validators.length; }
		};
	}
	
	/*static Argument[] get(@NotNull ArgumentValidator... validators) {
		Argument[] args = new Argument[validators.length];
		
		for(int i = 0; i < args.length; i++) {
			final int index = i;
			args[i] = new Argument() {
				@Override
				public boolean satisfiedBy(String[] args, int offset) {
					return validators[index].isValid(args[offset]);
				}
				
				@Override public int length() { return 1; }
			};
		}
		
		return args;
	}*/
	
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
		
		ArgumentValidator<String> ANY = arg -> arg;
		ArgumentValidator<Integer> INTEGER = arg -> noException(() -> Integer.parseInt(arg));
		ArgumentValidator<Float> DECIMAL = arg -> noException(() -> Float.parseFloat(arg));
		ArgumentValidator<Boolean> BOOLEAN = arg -> noException(() -> Boolean.parseBoolean(arg));
		ArgumentValidator<ServerPlayer> PLAYER = arg -> notNull(() -> ServerCore.getServer().getPlayerByName(arg));
		ArgumentValidator<Command> COMMAND = arg -> noException(() -> Enum.valueOf(Command.class, arg.toUpperCase()));
		ArgumentValidator<Float> CLOCK_TIME = arg -> noException(() -> {
			String[] parts = arg.split(":");
			int hour = Integer.parseInt(parts[0]);
			int min = Integer.parseInt(parts[1]);
			
			if(hour < 0 || hour >= 24 || min < 0 || min >= 60) throw new IllegalArgumentException();
			
			float time = hour + (min / 60f);
			
			time = (time + 24 - TimeOfDay.REL_START_TIME_OFFSET) % 24;
			return MyUtils.mapFloat(time, 0, 24, 0, TimeOfDay.SECONDS_IN_DAY);
		});
		
		static ArgumentValidator<String> exactString(boolean matchCase, String... matches) {
			return arg -> isTrue(() -> arg, theArg -> {
				for(String match: matches)
					if(matchCase ? theArg.equals(match) : theArg.equalsIgnoreCase(match))
						return true;
				
				return false;
			});
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
