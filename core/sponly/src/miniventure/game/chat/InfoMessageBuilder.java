package miniventure.game.chat;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.Nullable;

public class InfoMessageBuilder implements MessageBuilder {
	
	@FunctionalInterface
	public interface LineColorizer {
		InfoMessageLine getLine(String text);
	}
	
	private final Object lineLock = new Object();
	private final Object bufferLock;
	
	private Array<InfoMessageLine> lines;
	private StringBuilder str = new StringBuilder();
	
	private final LineColorizer colorizer;
	
	
	public InfoMessageBuilder(InfoMessageBuilder sync, LineColorizer colorizer) {
		this.colorizer = colorizer;
		this.bufferLock = sync.bufferLock;
		this.lines = sync.lines;
	}
	public InfoMessageBuilder(LineColorizer colorizer) {
		this.colorizer = colorizer;
		bufferLock = new Object();
		lines = new Array<>(InfoMessageLine.class);
	}
	
	@Override
	public void print(String text) { synchronized (lineLock) { str.append(text); } }
	@Override
	public void println(String line) {
		print(line);
		println();
	}
	@Override
	public void println() {
		String line;
		synchronized (lineLock) {
			line = str.toString();
			str = new StringBuilder();
		}
		
		synchronized (bufferLock) {
			lines.add(colorizer.getLine(line));
		}
	}
	
	@Nullable
	public InfoMessage flushMessage() {
		InfoMessageLine[] lines;
		synchronized (bufferLock) {
			lines = this.lines.toArray();
			this.lines.clear();
		}
		
		if(lines.length == 0) return null;
		
		return new InfoMessage(lines);
	}
	
}
