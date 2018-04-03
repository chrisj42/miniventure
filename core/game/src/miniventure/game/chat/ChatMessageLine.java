package miniventure.game.chat;

import com.badlogic.gdx.graphics.Color;

public class ChatMessageLine {
	
	public final String color;
	public final String line;
	
	private ChatMessageLine() { this((String)null, null); }
	public ChatMessageLine(Color color, String line) { this(color.toString(), line); }
	private ChatMessageLine(String color, String line) {
		this.color = color;
		this.line = line;
	}
	
}
