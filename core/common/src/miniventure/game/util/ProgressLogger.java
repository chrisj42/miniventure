package miniventure.game.util;

/**
 * This interface specifies a method of tracking a stack of "message" Strings.
 * This is useful to show the loading progress of something in a hierarchical fashion, or in other words it allows you to be more specific with what exactly is loading, without removing the text displaying the general category of what is loading.
 * 
 * For example, say you are loading a world. You can push the message "loading world", and then push another message "loading level 1". Then, push a third message "loading chunk __ of __".
 * When tiles are loaded, you can edit the last message to show as each chunk is loaded.
 * Then, you can pop the message, and edit the 2nd one to say "loading level 2", and repeat the process until all levels are loaded.
 * 
 * The whole time, the "loading world" message remains. I think this is much better than only showing the specific part being loaded.
 */
public interface ProgressLogger {
	
	/**
	 * adds a message to the message stack.
	 * 
	 * @param message the text of the message.
	 */
	void pushMessage(String message);
	
	/**
	 * edits the top message in the stack.
	 * 
	 * @param newMessage the new text for the top message.
	 */
	void editMessage(String newMessage);
	
	/**
	 * removes the top message from the message stack.
	 */
	void popMessage();
	
}
