package miniventure.game.ui;

import com.badlogic.gdx.graphics.Color;

public class TestScreen extends SceneRoot {
	
	public TestScreen() {
		super(new ListLayout(false, 3f), Color.ORANGE);
		
		addComponent(new Box(300, 100, Color.RED));
		addComponent(new Box(100, 200, Color.GREEN));
		addComponent(new Box(100, 100, Color.YELLOW));
	}
	
}
