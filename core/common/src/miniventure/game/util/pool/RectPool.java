package miniventure.game.util.pool;

import com.badlogic.gdx.math.Rectangle;

public class RectPool extends SyncPool<Rectangle> {
	
	public static final RectPool POOL = new RectPool();
	
	private RectPool() {
		super(12, 50);
	}
	
	@Override
	protected Rectangle newObject() {
		return new Rectangle();
	}
	
	public Rectangle obtain(float x, float y, float w, float h) {
		Rectangle r = obtain();
		r.set(x, y, w, h);
		return r;
	}
}
