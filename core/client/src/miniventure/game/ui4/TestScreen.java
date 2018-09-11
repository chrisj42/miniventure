package miniventure.game.ui4;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		setLayout(new LineLayout(false));
		add(LineLayout.createSpacer());
		// add(LineLayout.createSpacer(false, true));
		add(new Box(200, 100, Color.BLUE));
		add(LineLayout.createSpacer());
		// add(LineLayout.createSpacer(false, true));
		add(new Box(100, 200, Color.BLUE));
		add(LineLayout.createSpacer());
		// setBackground(Color.GRAY);
	}
	
}
