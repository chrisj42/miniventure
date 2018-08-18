package miniventure.game.ui3;

import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Component {
	
	// private static final Color DEFAULT_BACKGROUND = Color.LIGHT_GRAY;
	
	private Vector2 actualSize;
	private Vector2 prefSize;
	@NotNull private Vector2 position = new Vector2();
	
	@Nullable private Color backgroundColor = null;//DEFAULT_BACKGROUND;
	@NotNull private RelPos relPos = RelPos.CENTER;
	
	private Container parent;
	
	protected Component() {
		
	}
	
	public void render(Batch batch) {
		if(backgroundColor != null) {
			Vector2 size = getSize();
			Vector2 screenPos = getScreenPosition();
			MyUtils.fillRect(screenPos.x, screenPos.y, size.x, size.y, backgroundColor, batch);
		}
	}
	
	public void invalidate() {
		prefSize = null;
		if(parent != null)
			parent.invalidate(); // one of the children changed, so pref size will often have to be reevaluated.
	}
	
	
	public void setPosition(@NotNull Vector2 position) {
		this.position.set(position);
	}
	
	public Vector2 getRelativePosition() { return position.cpy(); }
	
	public Vector2 getScreenPosition() {
		if(parent == null)
			return getRelativePosition();
		
		return parent.getScreenPosition().add(position);
	}
	
	
	public final void setSize(Vector2 size) {
		if(actualSize == null) {
			if(size != null)
				actualSize = size.cpy();
		}
		else {
			if(size == null)
				actualSize = null;
			else
				actualSize.set(size);
		}
		configureForSize(getSize());
	}
	protected abstract void configureForSize(Vector2 size);
	
	public Vector2 getSize() {
		if(actualSize == null)
			actualSize = getPrefSize();
		return actualSize.cpy();
	}
	
	public Vector2 getPrefSize() {
		if(prefSize == null)
			prefSize = calcPrefSize(new Vector2());
		
		return prefSize.cpy();
	}
	
	protected abstract Vector2 calcPrefSize(Vector2 rt);
	
	@Nullable
	public Color getBackgroundColor() { return backgroundColor == null ? null : backgroundColor.cpy(); }
	public void setBackgroundColor(@Nullable Color color) { backgroundColor = color == null ? null : color.cpy(); }
	
	@NotNull
	public RelPos getRelPos() { return relPos; }
	public void setRelPos(@NotNull RelPos relPos) { this.relPos = relPos; invalidate(); }
	
	@Nullable
	public Container getParent() { return parent; }
	void setParent(Container parent) {
		if(this.parent == parent) return;
		
		if(this.parent != null)
			this.parent.removeChild(this);
		
		this.parent = parent;
		
		if(parent != null)
			parent.addChild(this);
		
		invalidate();
	}
}
