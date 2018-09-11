package miniventure.game.ui4;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
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
	public void render(Batch batch) {
		super.render(batch);
		MyUtils.drawRect(getScreenX(), getScreenY(), getSizeCache().getSize().x, getSizeCache().getSize().y, 2, Color.BLACK, batch);
	}
	
	@Override
	protected Vector2 calcMinSize(Vector2 rt) {
		return rt.set(10, 10);
	}
	
	@Override
	protected Vector2 calcPreferredSize(Vector2 rt) {
		return rt.set(width, height);
	}
}
