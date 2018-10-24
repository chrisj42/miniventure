package miniventure.game.chat;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class InfoMessageBuilder implements MessageBuilder {
	
	@FunctionalInterface
	public interface LineColorizer {
		InfoMessageLine getLine(String text);
	}
	
	private final Object lock;
	
	private Array<InfoMessageLine> lines;
	private StringBuilder str = new StringBuilder();
	
	private final LineColorizer colorizer;
	
	
	public InfoMessageBuilder(InfoMessageBuilder sync, LineColorizer colorizer) {
		this.colorizer = colorizer;
		this.lock = sync.lock;
		this.lines = sync.lines;
	}
	public InfoMessageBuilder(LineColorizer colorizer) {
		this.colorizer = colorizer;
		lock = new Object();
		lines = new Array<>(InfoMessageLine.class);
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
	public InfoMessage flushMessage() {
		InfoMessageLine[] lines;
		synchronized (lock) {
			lines = this.lines.toArray();
			this.lines.clear();
		}
		
		if(lines.length == 0) return null;
		
		return new InfoMessage(lines);
	}
	
}
