package miniventure.game.ui2;

import com.badlogic.gdx.math.Vector2;

public interface Layout {
	
	void layout(final float width, final float height, final Size prefSize, final Component[] components);
	
	void calcPreferredSize(final Vector2 v, final Component[] components);
	
}
