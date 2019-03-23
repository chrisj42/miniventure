package miniventure.game.chat.command;

import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.management.Config;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.management.TimeOfDay;

import org.jetbrains.annotations.NotNull;

public interface Argument {
	
	boolean satisfiedBy(@NotNull ServerWorld world, String[] args, int offset);
	int length();
	
	MapFunction<ServerPlayer, Boolean> SERVER_ONLY = player ->
		player == null || player.getWorld().getServer().isMultiplayer();
	
	/*static Argument get(@NotNull ArgValidator... validators) {
		return new Argument() {
			@Override
			public boolean satisfiedBy(@NotNull ServerWorld world, String[] args, int offset) {
				for(int i = 0; i < validators.length; i++)
					if(!validators[i].isValid(world, args[i]))
						return false;
				
				return true;
			}
			
			@Override
			public int length() { return validators.length; }
		};
	}*/
	
	static Argument varArg(ArgValidator validator) {
		return new Argument() {
			@Override
			public boolean satisfiedBy(@NotNull ServerWorld world, String[] args, int offset) {
				if(args.length - offset <= 0) return false; // must have at least one arg
				for(int i = offset; i < args.length; i++)
					if(!validator.isValid(world, args[i]))
						return false;
				return true;
			}
			
			@Override
			public int length() { return -1; }
		};
	}
	
	interface ArgValidator<T> extends Argument {
		
		@Override
		default boolean satisfiedBy(@NotNull ServerWorld world, String[] args, int offset) {
			return isValid(world, args[offset]);
		}
		
		@Override
		default int length() { return 1; }
		
		static <T> T notNull(FetchFunction<T> function, String error) throws IllegalArgumentException {
			T obj = function.get();
			if(obj == null)
				throw new IllegalArgumentException("value is null", new NullPointerException(error));
			return obj;
		}
		static <T> T noException(FetchFunction<T> function) throws IllegalArgumentException {
			return noException(function, "");
		}
		static <T> T noException(FetchFunction<T> function, String error) throws IllegalArgumentException {
			T obj;
			try {
				obj = function.get();
			} catch(Throwable t) {
				throw new IllegalArgumentException(error, t);
			}
			
			return obj;
		}
		
		SimpleArgValidator<String> ANY = arg -> arg;
		SimpleArgValidator<Integer> INTEGER = arg -> noException(() -> Integer.parseInt(arg),
			"arg '"+arg+"' is not an integer");
		SimpleArgValidator<Float> DECIMAL = arg -> noException(() -> Float.parseFloat(arg),
			"arg '"+arg+"' is not a decimal");
		SimpleArgValidator<Boolean> BOOLEAN = arg -> {
			if(arg == null || !arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false"))
				throw new IllegalArgumentException("arg '"+arg+"' is not a boolean");
			return Boolean.parseBoolean(arg);
		};
		ArgValidator<ServerPlayer> PLAYER = (world, arg) -> notNull(() -> world.getServer().getPlayerByName(arg),
			"player '"+arg+"' does not exist");
		SimpleArgValidator<Command> COMMAND = arg -> noException(() -> Enum.valueOf(Command.class, arg.toUpperCase()), 
			"command '"+arg+"' does not exist");
		SimpleArgValidator<Config> CONFIG_VALUE = arg -> notNull(() -> Config.valueOf(arg),
			"arg '"+arg+"' is not a valid config value");
		
		SimpleArgValidator<Float> CLOCK_DURATION = arg -> noException(() -> {
			String[] parts = arg.split(":");
			int hour = Integer.parseInt(parts[0]);
			int min = Integer.parseInt(parts[1]);
			
			if(hour < 0 || hour >= 24 || min < 0 || min >= 60)
				throw new IllegalArgumentException("hour and/or min is outside valid range");
			
			float time = hour + (min / 60f);
			
			return MyUtils.mapFloat(time, 0, 24, 0, TimeOfDay.SECONDS_IN_DAY);
		}, "arg is not a valid duration");
		
		SimpleArgValidator<Float> CLOCK_TIME = arg -> {
			float duration = CLOCK_DURATION.get(arg);
			float total = TimeOfDay.SECONDS_IN_DAY;
			return (duration + total - TimeOfDay.SECONDS_START_TIME_OFFSET) % total;
			//time = (time + 24 - ) % 24;
			//return MyUtils.mapFloat(time, 0, 24, 0, TimeOfDay.SECONDS_IN_DAY);
		};
		
		SimpleArgValidator<TimeOfDay> TIME_RANGE = arg -> noException(() -> TimeOfDay.valueOf(MyUtils.toTitleCase(arg)));
		
		SimpleArgValidator<Float> TIME = anyOf(CLOCK_TIME, map(TIME_RANGE, TimeOfDay::getStartOffsetSeconds));
		
		@SafeVarargs
		static <T> SimpleArgValidator<T> anyOf(SimpleArgValidator<T>... validators) {
			return arg -> {
				for(SimpleArgValidator<T> validator: validators) {
					if(validator.isValid(arg)) {
						return validator.get(arg);
					}
				}
				
				throw new IllegalArgumentException("no validators match");
			};
		}
		
		static <T1, T2> SimpleArgValidator<T2> map(SimpleArgValidator<T1> orig, MapFunction<T1, T2> mapper) { return arg -> mapper.get(orig.get(arg)); }
		
		static SimpleArgValidator<String> exactString(boolean matchCase, String... matches) { return exactString(str -> str, matchCase, matches); }
		static <T> SimpleArgValidator<T> exactString(MapFunction<String, T> resultMapper, boolean matchCase, String... matches) {
			return arg -> {
				for(String match : matches)
					if(matchCase ? arg.equals(match) : arg.equalsIgnoreCase(match))
						return resultMapper.get(arg);
				
				throw new IllegalArgumentException("arg '"+arg+"' must be one of "+
					ArrayUtils.arrayToString(matches, ", ", val -> "'"+val+'\''));
			};
		}
		
		T get(@NotNull ServerWorld world, String arg) throws IllegalArgumentException;
		
		default boolean isValid(@NotNull ServerWorld world, String arg) {
			try {
				get(world, arg);
			} catch(IllegalArgumentException ex) {
				return false;
			}
			
			return true;
		}
	}
	
	interface SimpleArgValidator<T> extends ArgValidator<T> {
		
		T get(String arg) throws IllegalArgumentException;
		
		@Override
		default T get(@NotNull ServerWorld world, String arg) throws IllegalArgumentException {
			return get(arg);
		}
		
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
