package miniventure.game.util.pool;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class SyncPool<T> extends Pool<T> {
	
	private final Object lock = new Object();
	
	public SyncPool() { super(); }
	public SyncPool(int initialCapacity) { super(initialCapacity); }
	public SyncPool(int initialCapacity, int max) { super(initialCapacity, max); }
	
	@Override
	public void free(T object) {
		synchronized (lock) {
			super.free(object);
		}
	}
	
	@Override
	public void freeAll(Array<T> objects) {
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
	public T obtain() {
		synchronized (lock) {
			return super.obtain();
		}
	}
	
}
