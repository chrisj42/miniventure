package miniventure.game.chat;

import java.io.PrintWriter;

public class ConsoleMessageBuilder implements MessageBuilder {
	
	private PrintWriter out;
	
	public ConsoleMessageBuilder(PrintWriter out) {
		this.out = out;
	}
	
	@Override
	public void println(String s) {
		out.println(s);
	}
	
	@Override
	public void print(String s) {
		out.print(s);
	}
	
	@Override
	public void println() { out.println(); }
	
	public void flush() { out.flush(); }
}
