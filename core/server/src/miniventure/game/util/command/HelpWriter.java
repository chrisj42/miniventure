package miniventure.game.util.command;

public class HelpWriter {
	
	private StringBuilder str;
	private int indents = 0;
	
	public HelpWriter() {
		str = new StringBuilder();
	}
	
	public HelpWriter line(String text) {
		for(int i = 0; i < indents; i++)
			str.append("\t");
		
		str.append(text);
		str.append(System.lineSeparator());
		
		return this;
	}
	
	public HelpWriter indent() { indents++; return this; }
	public HelpWriter unindent() { indents = Math.max(0, indents-1); return this; }
	
	public String toString() { return str.toString(); }
}
