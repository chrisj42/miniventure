package miniventure.game.ui;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public abstract class Component {
	
	// private Container parent;
	
	private Background background;
	private RelPos relPos; // if given more space than needed, position with this.
	private float x, y;
	
	public Component() { this(null); }
	public Component(Background background) {
		this.background = background;
	}
	
	protected void render(Batch batch, Vector2 parentPos) {
		// System.out.println("rendering component "+this);
		if(background != null) {
			// System.out.println("drawing background at "+scX()+','+scY()+" size "+getWidth()+','+getHeight());
			Vector2 size = getSize();
			background.draw(batch, 1f, x+parentPos.x, y+parentPos.y, size.x, size.y);
		}
	}
	
	protected final Vector2 getPosition() { return new Vector2(x, y); }
	protected void setPosition(Vector2 pos) { setPosition(pos.x, pos.y); }
	protected void setPosition(float x, float y) { this.x = x; this.y = y; }
	protected abstract Vector2 getSize();
	
	public Background getBackground() { return background; }
	public void setBackground(Background background) { this.background = background; }
	// public final float getX() { return x; }
	// public void setX(float x) { this.x = x; }
	// public final float getY() { return y; }
	
	// these get the actual on-screen position, for rendering.
	// public float scX() { return getX() + (parent == null ? 0 : parent.scX()); }
	// public float scY() { return getY() + (parent == null ? 0 : parent.scY()); }
	
	@NotNull
	protected RelPos getRelPos() { return relPos == null ? RelPos.CENTER : relPos; }
	protected void setRelPos(RelPos relPos) { this.relPos = relPos; }
	
	/*protected Container getParent() {
		return parent;
	}
	
	protected void setParent(Container parent) {
		this.parent = parent;
	}*/
	
	/*protected void setBounds(Rectangle rect) {
		if(relPos == null)
			relPos = RelPos.CENTER;
		Vector2 pos = relPos.positionRect(getSize(), rect);
		x = pos.x;
		y = pos.y;
	}*/
	// public void setY(float y) { this.y = y; }
	// public float getWidth() { return width; }
	// public void setWidth(float width) { this.width = width; }
	// public float getHeight() { return height; }
	// public void setHeight(float height) { this.height = height; }
}
