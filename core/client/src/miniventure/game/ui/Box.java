package miniventure.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Box extends Component {
	
	private final float width;
	private final float height;
	
	public Box(float width, float height) {
		this.width = width;
		this.height = height;
	}
	public Box(float width, float height, Color color) {
		this(width, height);
		setBackground(Background.fillColor(color));
	}
	
	@Override
	protected void calcPrefSize(Vector2 v) { v.set(width, height); }
	
	@Override
	protected void update() {}
}
