package miniventure.game.ui;

import com.badlogic.gdx.math.Vector2;

public interface Layout {
	
	void layout(Container container);
	
	Vector2 getSize(Container container);
	
}
