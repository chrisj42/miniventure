package miniventure.game.chat.command;

import java.util.Arrays;

import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.util.function.ValueMonoFunction;
import miniventure.game.world.Config;
import miniventure.game.world.TimeOfDay;
import miniventure.game.world.entity.mob.ServerPlayer;

import org.jetbrains.annotations.NotNull;

public interface Argument {
	
	boolean satisfiedBy(String[] args, int offset);
	int length();
	
	static Argument get(@NotNull ArgValidator... validators) {
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
	
	static Argument varArg(ArgValidator validator) {
		return new Argument() {
			@Override
			public boolean satisfiedBy(String[] args, int offset) {
				return validator.isValid(String.join(" ", Arrays.copyOfRange(args, offset, args.length)));
			}
			
			@Override
			public int length() { return -1; }
		};
	}
	
	interface ArgValidator<T> {
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
		static <T> T isTrue(ValueFunction<T> valueFunction, ValueMonoFunction<T, Boolean> boolFunction) throws IllegalArgumentException {
			T obj = valueFunction.get();
			if(!boolFunction.get(obj))
				throw new IllegalArgumentException();
			
			return obj;
		}
		
		
		ArgValidator<String> ANY = arg -> arg;
		ArgValidator<Integer> INTEGER = arg -> noException(() -> Integer.parseInt(arg));
		ArgValidator<Float> DECIMAL = arg -> noException(() -> Float.parseFloat(arg));
		ArgValidator<Boolean> BOOLEAN = arg -> noException(() -> Boolean.parseBoolean(arg));
		ArgValidator<ServerPlayer> PLAYER = arg -> notNull(() -> ServerCore.getServer().getPlayerByName(arg));
		ArgValidator<Command> COMMAND = arg -> noException(() -> Enum.valueOf(Command.class, arg.toUpperCase()));
		ArgValidator<Float> CLOCK_DURATION = arg -> noException(() -> {
			String[] parts = arg.split(":");
			int hour = Integer.parseInt(parts[0]);
			int min = Integer.parseInt(parts[1]);
			
			if(hour < 0 || hour >= 24 || min < 0 || min >= 60) throw new IllegalArgumentException();
			
			float time = hour + (min / 60f);
			
			return MyUtils.mapFloat(time, 0, 24, 0, TimeOfDay.SECONDS_IN_DAY);
		});
		ArgValidator<Float> CLOCK_TIME = arg -> {
			float duration = CLOCK_DURATION.get(arg);
			float total = TimeOfDay.SECONDS_IN_DAY;
			return (duration + total - TimeOfDay.SECONDS_START_TIME_OFFSET) % total;
			//time = (time + 24 - ) % 24;
			//return MyUtils.mapFloat(time, 0, 24, 0, TimeOfDay.SECONDS_IN_DAY);
		};
		ArgValidator<TimeOfDay> TIME_RANGE = arg -> noException(() -> TimeOfDay.valueOf(MyUtils.toTitleCase(arg)));
		ArgValidator<Float> TIME = anyOf(CLOCK_TIME, map(TIME_RANGE, TimeOfDay::getStartOffsetSeconds));
		ArgValidator<Config> CONFIG_VALUE = arg -> notNull(() -> Config.valueOf(arg));
		
		@SafeVarargs
		static <T> ArgValidator<T> anyOf(ArgValidator<T>... validators) {
			return arg -> {
				for(ArgValidator<T> validator: validators) {
					if(validator.isValid(arg)) {
						return validator.get(arg);
					}
				}
				
				throw new IllegalArgumentException();
			};
		}
		
		static <T1, T2> ArgValidator<T2> map(ArgValidator<T1> orig, ValueMonoFunction<T1, T2> mapper) { return arg -> mapper.get(orig.get(arg)); }
		
		static ArgValidator<String> exactString(boolean matchCase, String... matches) { return exactString(str -> str, matchCase, matches); }
		static <T> ArgValidator<T> exactString(ValueMonoFunction<String, T> mapper, boolean matchCase, String... matches) {
			return arg -> {
				for(String match : matches)
					if(matchCase ? arg.equals(match) : arg.equalsIgnoreCase(match))
						return mapper.get(arg);
				
				throw new IllegalArgumentException();
			};
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
