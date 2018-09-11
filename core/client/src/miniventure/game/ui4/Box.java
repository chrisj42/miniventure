package miniventure.game.ui4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Box extends Component {
	
	private final float width;
	private final float height;
	
	public Box(float width, float height, Color background) {
		this.width = width;
		this.height = height;
		setBackground(background);
	}
	
	@Override
	protected Vector2 calcPreferredSize(Vector2 rt) {
		return rt.set(width, height);
	}
}
