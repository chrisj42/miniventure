package miniventure.game.chat;

public class ChatMessage {
	
	public final ChatMessageLine[] lines;
	
	private ChatMessage() { this(null); }
	public ChatMessage(ChatMessageLine[] lines) {
		this.lines = lines;
	}
	
}
