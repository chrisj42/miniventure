package miniventure.gentest.util;

import org.jetbrains.annotations.NotNull;

public class StringField extends ValueField<String> {
	
	public StringField() { this(""); }
	public StringField(String value) {
		super(value);
	}
	
	public StringField(String value, int columns) {
		super(value, columns);
	}
	
	@Override
	protected void onTextChange() {}
	
	@NotNull
	@Override
	public String getValue() { return getText(); }
}
