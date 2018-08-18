package miniventure.game.ui3;

import com.badlogic.gdx.math.Vector2;

public abstract class Component {
	
	private Vector2 actualSize, prefSize;
	
	
	
	protected void invalidate() {
		prefSize = null;
	}
	
	final void setSize(Vector2 size) {
		actualSize.set(size);
		configureForSize(size);
	}
	protected abstract void configureForSize(Vector2 size);
	
	public Vector2 getPrefSize() {
		if(prefSize == null)
			prefSize = calcPrefSize();
		
		return prefSize.cpy();
	}
	
	protected abstract Vector2 calcPrefSize();
}
