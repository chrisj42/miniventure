package miniventure.game.ui;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		root.setLayout(new VerticalListLayout(RelPos.LEFT, 20));
		root.setBackground(Background.fillColor(Color.RED));
		root.addComponent(new Box(120, 100, Color.GREEN));
		root.addComponent(new Box(100, 100, Color.BLUE));
		root.addComponent(new Box(100, 120, Color.YELLOW));
	}
	
}
