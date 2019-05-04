package miniventure.game.chat;

public interface MessageBuilder {
	
	void println(String s);
	void print(String s);
	
	default void println(Object o) { println(o == null ? "null" : o.toString()); }
	default void print(Object o) { print(o == null ? "null" : o.toString()); }
	
	void println();
}
