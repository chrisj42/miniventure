package miniventure.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public abstract class Component {
	
	private Background background;
	
	public Component() { this(null); }
	public Component(Background background) {
		this.background = background;
	}
	
	protected void render(Batch batch) {
		// System.out.println("rendering component "+this);
		if(background != null) {
			// System.out.println("drawing background at "+scX()+','+scY()+" size "+getWidth()+','+getHeight());
			Vector2 size = getSize();
			Vector2 pos = getPosition();
			background.draw(batch, 1f, pos.x, pos.y, size.x, size.y);
		}
	}
	
	protected abstract Vector2 getPosition();
	protected abstract Vector2 getSize();
	
	public Background getBackground() { return background; }
	public void setBackground(Background background) { this.background = background; }
	// public float getX() { return x; }
	// public void setX(float x) { this.x = x; }
	// public float getY() { return y; }
	// public void setY(float y) { this.y = y; }
	// public float getWidth() { return width; }
	// public void setWidth(float width) { this.width = width; }
	// public float getHeight() { return height; }
	// public void setHeight(float height) { this.height = height; }
}
