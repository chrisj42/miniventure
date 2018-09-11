package miniventure.game.ui4;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		setLayout(new LineLayout(true, 10, false));
		add(new Box(100, 100, Color.BLUE));
		add(new Box(100, 200, Color.BLUE));
		// setBackground(Color.GRAY);
	}
	
}
