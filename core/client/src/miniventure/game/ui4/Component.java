package miniventure.game.ui4;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Component {
	
	// next feature: add component padding.
	
	@NotNull private final SizeCache sizeCache = new SizeCache();
	@NotNull private Vector2 position = new Vector2();
	
	private Container parent;
	
	private boolean valid = false;
	
	@Nullable private Color background = null;
	
	Component() {
		
	}
	
	boolean isValid() { return valid; }
	
	public void invalidate() {
		valid = false;
		sizeCache.invalidateSizes();
		invalidateParent();
	}
	
	protected void invalidateParent() {
		if(parent != null && parent.isValid())
			parent.invalidate();
	}
	
	public void validate() {
		if(valid) return;
		layout();
		valid = true;
		if(this instanceof Screen)
			System.out.println();
	}
	
	// does little for components (except maybe internal layout, not represented as Components), but containers will rely heavily on it.
	public void layout() {}
	
	public void render(Batch batch) {
		if(background != null)
			MyUtils.fillRect(getScreenX(), getScreenY(), sizeCache.size.x, sizeCache.size.y, background, batch);
	}
	
	protected Container getParent() { return parent; }
	void setParent(Container parent) { this.parent = parent; }
	
	public void setBackground(@Nullable Color color) { this.background = color; }
	
	
	public void setPosition(Vector2 pos) { setPosition(pos.x, pos.y); }
	public void setPosition(float x, float y) {
		System.out.println("set position of "+this+" to "+x+","+y);
		if(position.x != x || position.y != y) {
			position.set(x, y);
			invalidateParent();
		}
	}
	
	public Vector2 getPosition() { return getPosition(new Vector2()); }
	public Vector2 getPosition(Vector2 rt) { return rt == null ? getPosition() : rt.set(position); }
	
	public Vector2 getPositionOnScreen() { return getPositionOnScreen(new Vector2()); }
	public Vector2 getPositionOnScreen(Vector2 rt) { return rt == null ? getPositionOnScreen() : rt.set(getScreenX(), getScreenY()); }
	
	public float getScreenX() { return position.x + (parent == null ? 0 : ((Component)parent).getScreenX()); }
	public float getScreenY() { return position.y + (parent == null ? 0 : ((Component)parent).getScreenY()); }
	
	
	@NotNull
	public SizeCache getSizeCache() { return sizeCache; }
	
	
	protected Vector2 calcMinSize(Vector2 rt) { return calcPreferredSize(rt); }
	
	protected Vector2 calcPreferredSize(Vector2 rt) { return rt.set(1, 1); }
	
	// note: I'm going to have to make sure to account for numeric overflow when adding sizes together, such as when calculating the size of containers.
	protected Vector2 calcMaxSize(Vector2 rt) { return rt.set(1E20f, 1E20f/*(float)Math.sqrt(Float.MAX_VALUE), (float)Math.sqrt(Float.MAX_VALUE)*/); }
	
	// a class that extends container, that represents a root screen, will start validation by setting its size to the size of the application window.
	// it will not matter what the pref, min, or max size of the root screen is.
	
	public class SizeCache {
		
		private Vector2 minSize, prefSize, maxSize; // caches
		private boolean minSizeSet, prefSizeSet, maxSizeSet;
		
		@NotNull private Vector2 size = new Vector2(); // the actual size
		
		public SizeCache() {
			
		}
		
		void invalidateSizes() {
			if(!minSizeSet) minSize = null;
			if(!maxSizeSet) maxSize = null;
			if(!prefSizeSet) prefSize = null;
		}
		
		public void setSize(@NotNull Vector2 size) { setSize(size.x, size.y); }
		public void setSize(float width, float height) {
			System.out.println("set size of "+Component.this+" to "+width+","+height);
			size.set(width, height);
			invalidateParent();
		}
		
		public void setMinSize(float width, float height) {
			minSizeSet = true;
			if(minSize == null) minSize = new Vector2(width, height);
			else minSize.set(width, height);
			invalidate();
		}
		public void setMinSize(@Nullable Vector2 size) {
			if(size == null) {
				minSizeSet = false;
				invalidate();
			}
			else setMinSize(size.x, size.y);
		}
		
		public void setPreferredSize(float width, float height) {
			prefSizeSet = true;
			if(prefSize == null) prefSize = new Vector2(width, height);
			else prefSize.set(width, height);
			invalidate();
		}
		public void setPreferredSize(@Nullable Vector2 size) {
			if(size == null) {
				prefSizeSet = false;
				invalidate();
			}
			else setPreferredSize(size.x, size.y);
		}
		
		public void setMaxSize(float width, float height) {
			maxSizeSet = true;
			if(maxSize == null) maxSize = new Vector2(width, height);
			else maxSize.set(width, height);
			invalidate();
		}
		public void setMaxSize(@Nullable Vector2 size) {
			if(size == null) {
				maxSizeSet = false;
				invalidate();
			}
			else setMaxSize(size.x, size.y);
		}
		
		@NotNull public Vector2 getSize() { return getSize(new Vector2()); }
		@NotNull public Vector2 getSize(Vector2 rt) { return rt == null ? getSize() : rt.set(size); }
		
		public Vector2 getMinSize() { return getMinSize(new Vector2()); }
		public Vector2 getMinSize(Vector2 rt) {
			if(minSize == null)
				minSize = calcMinSize(new Vector2());
			return (rt == null ? new Vector2() : rt).set(minSize);
		}
		
		public Vector2 getPreferredSize() { return getPreferredSize(new Vector2()); }
		public Vector2 getPreferredSize(Vector2 rt) {
			if(rt == null) return getPreferredSize();
			if(prefSize == null)
				prefSize = calcPreferredSize(new Vector2());
			return rt.set(prefSize);
		}
		
		public Vector2 getMaxSize() { return getMaxSize(new Vector2()); }
		public Vector2 getMaxSize(Vector2 rt) {
			if(maxSize == null)
				maxSize = calcMaxSize(new Vector2());
			return (rt == null ? new Vector2() : rt).set(maxSize);
		}
		
		public boolean isMinSizeSet() { return minSizeSet; }
		public boolean isPreferredSizeSet() { return prefSizeSet; }
		public boolean isMaxSizeSet() { return maxSizeSet; }
	}
}
