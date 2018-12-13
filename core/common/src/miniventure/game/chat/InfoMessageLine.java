package miniventure.game.chat;

import com.badlogic.gdx.graphics.Color;

public class InfoMessageLine {
	
	public final String color;
	public final String line;
	
	private InfoMessageLine() { this((String)null, null); }
	public InfoMessageLine(Color color, String line) { this(color.toString(), line); }
	private InfoMessageLine(String color, String line) {
		this.color = color;
		this.line = line;
	}
	
}
