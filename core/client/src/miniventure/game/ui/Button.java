package miniventure.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Button extends Container {
	
	// contains a label.
	private Label label;
	
	public Button(String text) {
		setLayout(new LineLayout(false));
		label = new Label(text);
		add(label);
		setBackground(Color.FIREBRICK);
	}
	
	@Override
	protected Vector2 calcMinSize(Vector2 rt) {
		return calcPreferredSize(rt);
	}
	
	@Override
	protected Vector2 calcPreferredSize(Vector2 rt) {
		label.calcPreferredSize(rt);
		return rt.add(20, 15);
	}
	
	@Override
	protected Vector2 calcMaxSize(Vector2 rt) {
		return calcPreferredSize(rt);
	}
}
