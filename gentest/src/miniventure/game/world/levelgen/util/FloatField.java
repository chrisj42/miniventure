package miniventure.game.world.levelgen.util;

import java.text.DecimalFormat;

import org.jetbrains.annotations.NotNull;

public class FloatField extends ValueField<Float> {
	
	public static final DecimalFormat format = new DecimalFormat("0.###");
	
	private float lastValidValue;
	private final float minVal;
	
	public FloatField() { this(0); }
	public FloatField(float value) { this(value, 3, Float.MIN_VALUE); }
	public FloatField(float value, float minVal) { this(value, 3, minVal); }
	public FloatField(int columns) { this(0, columns, Float.MIN_VALUE); }
	
	public FloatField(float value, int columns, float minVal) {
		super(value, columns, format::format);
		this.lastValidValue = value;
		this.minVal = minVal;
	}
	
	@Override
	protected void onTextChange() {
		String text = getText();
		try {
			lastValidValue = Math.abs(Float.parseFloat(text));
		} catch(NumberFormatException ignored) {}
	}
	
	@Override @NotNull
	public Float getValue() { return lastValidValue; }
	
	@Override
	public void setValue(@NotNull Float value) {
		value = Math.max(minVal, value);
		super.setValue(value);
		lastValidValue = value;
	}
}
