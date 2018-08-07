package miniventure.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Box extends Component {
	
	private final float x;
	private final float y;
	private final float width;
	private final float height;
	
	public Box(float x, float y, float width, float height, Color background) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		setBackground(Background.fillColor(background));
	}
	
	@Override
	protected Vector2 getPosition() {
		return new Vector2(x, y);
	}
	
	@Override
	protected Vector2 getSize() {
		return new Vector2(width, height);
	}
}
