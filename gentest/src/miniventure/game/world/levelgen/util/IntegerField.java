package miniventure.game.world.levelgen.util;

import org.jetbrains.annotations.NotNull;

public class IntegerField extends ValueField<Integer> {
	
	private int lastValidValue;
	private final int minVal;
	
	public IntegerField() { this(0); }
	public IntegerField(int value) { this(value, 3, Integer.MIN_VALUE); }
	public IntegerField(int value, int minVal) { this(value, 3, minVal); }
	public IntegerField(Integer columns) { this(0, columns, Integer.MIN_VALUE); }
	
	public IntegerField(int value, int columns, int minVal) {
		super(value, columns);
		this.lastValidValue = value;
		this.minVal = minVal;
	}
	
	@Override
	protected void onTextChange() {
		String text = getText();
		try {
			lastValidValue = Math.max(minVal, Integer.parseInt(text));
		} catch(NumberFormatException ignored) {}
	}
	
	@Override @NotNull
	public Integer getValue() { return lastValidValue; }
	
	@Override
	public void setValue(@NotNull Integer value) {
		value = Math.max(minVal, value);
		super.setValue(value);
		lastValidValue = value;
	}
}
