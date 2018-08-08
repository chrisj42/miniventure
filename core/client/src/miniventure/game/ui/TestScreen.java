package miniventure.game.ui;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends Screen {
	
	public TestScreen() {
		root.setLayout(new VerticalListLayout(RelPos.LEFT, 20));
		root.setBackground(Background.fillColor(Color.RED));
		root.addComponent(new Box(120, 20, Color.GREEN));
		root.addComponent(new Box(100, 10, Color.BLUE));
		root.addComponent(new Box(100, 12, Color.YELLOW));
		root.addComponent(new Label(80, true, "Hello everyone people yes people. All the peoples. And other things too."));
		root.addComponent(new Box(10, 20, Color.BROWN).setFillParent(true, false));
	}
	
}
