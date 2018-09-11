package miniventure.game.ui4;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		setLayout(new LineLayout(true, true));
		// add(LineLayout.createSpacer());
		add(new Box(200, 100, Color.BLUE));
		// add(LineLayout.createSpacer());
		add(new Box(100, 200, Color.BLUE));
		// add(LineLayout.createSpacer());
		// setBackground(Color.GRAY);
	}
	
}
