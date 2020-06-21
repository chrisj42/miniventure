package miniventure.game.world.management;

import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Argument.ArgValidator;
import miniventure.game.util.customenum.GenericEnum;

import org.jetbrains.annotations.NotNull;

public class Config<T> extends GenericEnum<T, Config<T>> {
	
	public static final Config<Boolean> DaylightCycle = new Config<>(ArgValidator.BOOLEAN, true);
	
	private T value;
	private final ArgValidator<T> stringParser;
	
	private Config(ArgValidator<T> stringParser, T defaultValue) {
		this.stringParser = stringParser;
		value = defaultValue;
	}
	
	public T get() { return value; }
	public void set(T value) { this.value = value; }
	public boolean set(@NotNull WorldManager world, String stringValue, MessageBuilder err) {
		// if(!stringParser.isValid(world, stringValue)) return false;
		try {
			set(stringParser.get(world, stringValue));
		} catch(IllegalArgumentException e) {
			err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
}
