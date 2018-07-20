package miniventure.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public abstract class Component {
	
	private final Size minSize, prefSize, maxSize;
	private final Vector2 size = new Vector2();
	private final Vector2 pos = new Vector2(); // note that this is relative to the parent position.
	
	private Container parent;
	
	@Nullable private Background background;
	
	// private RelPos alignment = RelPos.CENTER; // this will not have a specific or particular use, but rather for whatever wants to take a look. components may use it to align themselves inside a larger area than they contain (but that they own), or layouts may use it to decide where to put the component in a list in relation to other components.
	
	Component() {
		minSize = new Size(this::calcMinSize);
		prefSize = new Size(this::calcPrefSize);
		maxSize = new Size(this::calcMaxSize);
	}
	
	protected void calcMinSize(Vector2 v) { calcPrefSize(v); }
	protected abstract void calcPrefSize(Vector2 v);
	protected void calcMaxSize(Vector2 v) { v.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); }
	
	void invalidate() {
		minSize.invalidate();
		prefSize.invalidate();
		maxSize.invalidate();
		if(parent != null)
			parent.invalidate();
	}
	
	public Container getParent() { return parent; }
	void setParent(Container container) { this.parent = container; }
	
	protected abstract void update(); // for container, layout here, then call child updates.
	
	// later, alpha..? also later, have background color.
	protected void render(Batch batch) {
		// System.out.println("rendering component "+this);
		if(background != null) {
			// System.out.println("drawing background at "+scX()+','+scY()+" size "+getWidth()+','+getHeight());
			background.draw(batch, 1f, scX(), scY(), getWidth(), getHeight());
		}
	}
	// Also have isOpaque so I can check if a component is entirely obscured, and not render it if that's the case.
	// idk if that's actually worth it though... I might never do that.
	
	public void setBackground(@Nullable Background background) { this.background = background; }
	
	public final Size getMinSize() { return minSize; }
	public final Size getPrefSize() { return prefSize; }
	public final Size getMaxSize() { return maxSize; }
	
	
	public float getX() { return pos.x; }
	public float getY() { return pos.y; }
	// these get the actual on-screen position, for rendering.
	public float scX() { return getX() + (parent == null ? 0 : parent.scX()); }
	public float scY() { return getY() + (parent == null ? 0 : parent.scY()); }
	public void setX(float x) { pos.x = x; }
	public void setY(float y) { pos.y = y; }
	
	public float getWidth() { return size.x; }
	public float getHeight() { return size.y; }
	public void setWidth(float width) { size.x = width; }
	public void setHeight(float height) { size.y = height; }
	
	public void setPosition(float x, float y) { setX(x); setY(y); }
	public void setSize(float width, float height) { setWidth(width); setHeight(height); }
	
	public void setBounds(float x, float y, float width, float height) {
		setPosition(x, y);
		setSize(width, height);
	}
}
