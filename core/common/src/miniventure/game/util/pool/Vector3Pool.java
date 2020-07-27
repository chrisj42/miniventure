package miniventure.game.util.pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Vector3Pool extends SyncPool<Vector3> {
	
	public static final Vector3Pool POOL = new Vector3Pool();
	
	private Vector3Pool() {
		super();
	}
	
	@Override
	protected Vector3 newObject() {
		return new Vector3();
	}
	
	public Vector3 obtain(float x, float y, float z) {
		Vector3 v = obtain();
		v.set(x, y, z);
		return v;
	}
	public Vector3 obtain(Vector2 v2, float z) {
		Vector3 v = obtain();
		v.set(v2, z);
		return v;
	}
	public Vector3 obtain(Vector3 v3) {
		Vector3 v = obtain();
		v.set(v3);
		return v;
	}
}
