package miniventure.game.chat;

import java.awt.Color;

public class InfoMessageLine {
	
	public final int color;
	public final String line;
	
	private InfoMessageLine() { this(0, null); }
	public InfoMessageLine(Color color, String line) { this(color.getRGB(), line); }
	private InfoMessageLine(int color, String line) {
		this.color = color;
		this.line = line;
	}
	
}
