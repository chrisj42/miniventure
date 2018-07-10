package miniventure.game.world.levelgen.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.jetbrains.annotations.NotNull;

public class StickyValidatedField<T> extends ValidatedField<T> {
	
	@FunctionalInterface
	public interface Stringifier<T> {
		String get(T value);
	}
	
	@FunctionalInterface
	public interface ValueFetcher<T> {
		@NotNull T get(String input, T prev);
	}
	
	@NotNull private final Stringifier<T> stringifier;
	@NotNull private final ValueFetcher<T> validValueFetcher;
	@NotNull private T lastValidValue;
	
	@SafeVarargs
	public StickyValidatedField(@NotNull T initialValue, @NotNull StringConverter<T> converter, @NotNull Validator<? super T>... validators) {
		this(initialValue, Object::toString, converter, validators);
	}
	@SafeVarargs
	public StickyValidatedField(@NotNull T initialValue, @NotNull Stringifier<T> stringifier, @NotNull StringConverter<T> converter, @NotNull Validator<? super T>... validators) {
		this(initialValue, stringifier, converter, (text, last) -> last, validators);
	}
	@SafeVarargs
	public StickyValidatedField(@NotNull T initialValue, @NotNull Stringifier<T> stringifier, @NotNull StringConverter<T> converter, @NotNull ValueFetcher<T> validValueFetcher, @NotNull Validator<? super T>... validators) {
		super(converter, validators);
		this.stringifier = stringifier;
		this.validValueFetcher = validValueFetcher;
		lastValidValue = initialValue;
		
		addListener(field -> {
			T val = field.getValue();
			if(field.getValid() && val != null)
				lastValidValue = val;
		});
		
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setText(String.valueOf(stringifier.get(validValueFetcher.get(getText(), lastValidValue))));
				revalidateText();
			}
		});
	}
	
	@Override @NotNull
	public T getValue() { return lastValidValue; }
	
	public void setValue(@NotNull T value) {
		this.lastValidValue = value;
		setText(stringifier.get(value));
	}
}
