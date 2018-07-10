package miniventure.game.world.levelgen.util;

import java.text.DecimalFormat;

import org.jetbrains.annotations.NotNull;

public class FloatField extends ValueField<Float> {
	
	public static final DecimalFormat format = new DecimalFormat("0.###");
	
	private float lastValidValue;
	
	public FloatField() { this(0f); }
	public FloatField(float value) { this(value, 4); }
	public FloatField(int columns) { this(0f, columns); }
	
	public FloatField(float value, int columns) {
		super(value, columns, format::format);
		this.lastValidValue = value;
	}
	
	@Override
	protected void onTextChange() {
		String text = getText();
		try {
			lastValidValue = Float.parseFloat(text);
		} catch(NumberFormatException ignored) {}
	}
	
	@Override @NotNull
	public Float getValue() { return lastValidValue; }
	
	@Override
	public void setValue(@NotNull Float value) {
		super.setValue(value);
		lastValidValue = value;
	}
}
