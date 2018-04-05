package miniventure.game.chat;

// this class is sent by the server only, and it will usually be used to send the output of commands, as many commands may give multiple lines of output.

public class InfoMessage {
	
	public final InfoMessageLine[] lines;
	
	private InfoMessage() { this(null); }
	public InfoMessage(InfoMessageLine[] lines) {
		this.lines = lines;
	}
	
}
