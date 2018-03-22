package miniventure.game.util;

public class SynchronizedAccessor<T> {
	
	@FunctionalInterface
	public interface SyncAccess<ST> {
		void access(ST syncedObject);
	}
	
	@FunctionalInterface
	public interface SyncReturn<RT, ST> {
		RT get(ST syncedObject);
	}
	
	
	private final T obj;
	
	public SynchronizedAccessor(T obj) {
		this.obj = obj;
	}
	
	public void access(SyncAccess<T> action) {
		synchronized (obj) {
			action.access(obj);
		}
	}
	
	public <R> R get(SyncReturn<R, T> valueFunction) {
		R value;
		synchronized (obj) {
			value = valueFunction.get(obj);
		}
		
		return value;
	}
	
}
