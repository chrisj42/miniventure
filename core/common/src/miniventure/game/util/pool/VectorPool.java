package miniventure.game.util.pool;

import com.badlogic.gdx.math.Vector2;

public class VectorPool extends SyncPool<Vector2> {
	
	public static final VectorPool POOL = new VectorPool();
	
	private VectorPool() {
		super();
	}
	
	@Override
	protected Vector2 newObject() {
		return new Vector2();
	}
	
	public Vector2 obtain(float x, float y) {
		Vector2 v = obtain();
		v.set(x, y);
		return v;
	}
	public Vector2 obtain(Vector2 v2) {
		Vector2 v = obtain();
		v.set(v2);
		return v;
	}
}
