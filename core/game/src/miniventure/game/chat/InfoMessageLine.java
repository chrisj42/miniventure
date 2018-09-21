package miniventure.game.chat;

import java.awt.Color;

public class InfoMessageLine {
	
	public final Integer color;
	public final String line;
	
	private InfoMessageLine() { this((Integer)null, null); }
	public InfoMessageLine(Color color, String line) { this(color.getRGB(), line); }
	private InfoMessageLine(Integer color, String line) {
		this.color = color;
		this.line = line;
	}
	
}
