package miniventure.game.ui3;

import com.badlogic.gdx.math.Vector2;

public interface Layout {
	
	default Vector2 calcPrefSize(Component[] children) { return calcPrefSize(children, new Vector2()); }
	Vector2 calcPrefSize(Component[] children, Vector2 rt);
	
	void applyLayout(Vector2 size, Component[] children);
}
