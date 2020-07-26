package miniventure.game.util.pool;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RectPool extends Pool<Rectangle> {
	
	public static final RectPool POOL = new RectPool();
	
	private final Object lock = new Object();
	
	private RectPool() {
		super(12, 50);
	}
	
	@Override
	protected Rectangle newObject() {
		return new Rectangle();
	}
	
	@Override
	public void free(Rectangle object) {
		synchronized (lock) {
			super.free(object);
		}
	}
	
	@Override
	public void freeAll(Array<Rectangle> objects) {
		synchronized (lock) {
			super.freeAll(objects);
		}
	}
	
	@Override
	public void clear() {
		synchronized (lock) {
			super.clear();
		}
	}
	
	@Override
	public int getFree() {
		synchronized (lock) {
			return super.getFree();
		}
	}
	
	@Override
	public Rectangle obtain() {
		synchronized (lock) {
			return super.obtain();
		}
	}
	
	public Rectangle obtain(float x, float y, float w, float h) {
		Rectangle r = obtain();
		r.set(x, y, w, h);
		return r;
	}
}
