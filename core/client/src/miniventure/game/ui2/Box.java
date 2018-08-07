package miniventure.game.ui2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Box extends Component {
	
	private final float width;
	private final float height;
	
	public Box(float width, float height) {
		this.width = width;
		this.height = height;
		setSize(width, height);
	}
	public Box(float x, float y, float width, float height) {
		this(width, height);
		setPosition(x, y);
	}
	public Box(float width, float height, Color color) {
		this(width, height);
		setBackground(Background.fillColor(color));
	}
	public Box(float x, float y, float width, float height, Color color) {
		this(width, height, color);
		setPosition(x, y);
	}
	
	@Override
	protected void calcPrefSize(Vector2 v) { v.set(width, height); }
	
	@Override
	protected void update() { setSize(width, height); }
}
