package miniventure.game.ui;

import com.badlogic.gdx.math.Vector2;

public class Label extends Component {
	
	private final float width;
	private final String text;
	
	public Label(float width, String text) {
		this.width = width;
		this.text = text;
	}
	
	@Override
	protected Vector2 getSize() {
		return null;
	}
}
