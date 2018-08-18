package miniventure.game.ui3;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public abstract class Component {
	
	private Vector2 actualSize;
	private Vector2 prefSize;
	@NotNull private Vector2 position = new Vector2();
	
	protected Component() {
		
	}
	
	public abstract void render(Batch batch);
	
	public void invalidate() {
		prefSize = null;
	}
	
	
	public void setPosition(@NotNull Vector2 position) {
		this.position.set(position);
	}
	
	public Vector2 getPosition() { return position.cpy(); }
	
	
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
			prefSize = calcPrefSize();
		
		return prefSize.cpy();
	}
	
	protected abstract Vector2 calcPrefSize();
}
