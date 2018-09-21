package miniventure.game.ui;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		setLayout(new LineLayout(true));
		add(LineLayout.createSpacer());
		for(int i = 0; i < 3; i++) {
			add(new Button("Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me Click Me "+(i+1)));
			add(LineLayout.createSpacer());
		}
	}
	
}
