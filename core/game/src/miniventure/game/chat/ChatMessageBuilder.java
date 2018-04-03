package miniventure.game.chat;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class ChatMessageBuilder implements MessageBuilder {
	
	@FunctionalInterface
	public interface LineColorizer {
		ChatMessageLine getLine(String text);
	}
	
	private final Object lock;
	
	private Array<ChatMessageLine> lines;
	private StringBuilder str = new StringBuilder();
	
	private final LineColorizer colorizer;
	
	
	public ChatMessageBuilder(ChatMessageBuilder sync, LineColorizer colorizer) {
		this.colorizer = colorizer;
		this.lock = sync.lock;
		this.lines = sync.lines;
	}
	public ChatMessageBuilder(LineColorizer colorizer) {
		this.colorizer = colorizer;
		lock = new Object();
		lines = new Array<>(ChatMessageLine.class);
	}
	
	public void print(String text) { str.append(text); }
	public void println(String line) {
		print(line);
		println();
	}
	public void println() {
		synchronized (lock) {
			lines.add(colorizer.getLine(str.toString()));
		}
		str = new StringBuilder();
	}
	
	@Nullable
	public ChatMessage flushMessage() {
		ChatMessageLine[] lines;
		synchronized (lock) {
			lines = this.lines.toArray();
			this.lines.clear();
		}
		
		if(lines.length == 0) return null;
		
		return new ChatMessage(lines);
	}
	
}
