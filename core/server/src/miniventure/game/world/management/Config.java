package miniventure.game.world.management;

import java.lang.reflect.Field;
import java.util.HashMap;

import miniventure.game.chat.command.Argument.ArgValidator;

import org.jetbrains.annotations.NotNull;

public class Config<T> {
	
	private static int curOrdinal = 0;
	
	/* --- TYPE DEFINITIONS --- */
	
	
	public static final Config<Boolean> DaylightCycle = new Config<>(ArgValidator.BOOLEAN, true);
	
	//public static final Config<> _ = new Config<>();
	
	
	/* --- ENUMERATION SETUP --- */
	
	
	private static final HashMap<String, Config<?>> nameToValue = new HashMap<>();
	private static final HashMap<Config<?>, String> valueToName = new HashMap<>();
	static {
		for(Field field: Config.class.getDeclaredFields()) {
			if(field.getType() != Config.class) continue;
			try {
				valueToName.put((Config<?>) field.get(null), field.getName());
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		for(Config<?> value: valueToName.keySet())
			nameToValue.put(valueToName.get(value).toLowerCase(), value);
	}
	
	public static Config<?>[] values() { return valueToName.keySet().toArray(new Config[0]); }
	public static Config valueOf(String str) { return nameToValue.get(str.toLowerCase()); }
	
	public static final Config<?>[] values = values();
	
	private final int ordinal;
	
	public String name() { return valueToName.get(this); }
	public int ordinal() { return ordinal; }
	
	@Override public int hashCode() { return ordinal(); }
	@Override public boolean equals(Object other) { return other instanceof Config && ((Config)other).ordinal() == ordinal(); }
	@Override public String toString() { return name(); }
	
	
	
	/* --- INSTANCE DEFINITIONS --- */
	
	
	private T value;
	private final ArgValidator<T> stringParser;
	
	private Config(ArgValidator<T> stringParser, T defaultValue) {
		this.stringParser = stringParser;
		this.ordinal = curOrdinal;
		curOrdinal++;
		value = defaultValue;
	}
	
	public T get() { return value; }
	public void set(T value) { this.value = value; }
	public boolean set(@NotNull ServerWorld world, String stringValue) {
		if(!stringParser.isValid(world, stringValue)) return false;
		set(stringParser.get(world, stringValue));
		return true;
	}
	
}
