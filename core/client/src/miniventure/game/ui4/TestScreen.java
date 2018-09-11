package miniventure.game.ui4;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		setLayout(new LineLayout(false));
		add(LineLayout.createHorizontalSpacer());
		for(int i = 0; i < 3; i++) {
			Container c = new Container(new LineLayout(true));
			c.add(LineLayout.createVerticalSpacer());
			for(int j = i; j < 4; j++) {
				c.add(new Box(200, 50, Color.GREEN));
				c.add(LineLayout.createVerticalSpacer());
			}
			add(c);
			add(LineLayout.createHorizontalSpacer());
		}
	}
	
}
