package miniventure.game.util;

public interface Blinker {
	
	void update(float delta);
	boolean shouldRender();
	void reset();
	
}
