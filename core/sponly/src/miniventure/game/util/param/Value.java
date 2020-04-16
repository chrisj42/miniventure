package miniventure.game.util.param;

public class Value<T> {
	
	private final Param<T> param;
	private final T value;
	
	Value(Param<T> param, T value) {
		this.param = param;
		this.value = value;
	}
	
	public T get() {
		return value;
	}
	
	Param<T> getParam() {
		return param;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Value)) return false;
		
		Value<?> other = (Value<?>) o;
		
		return param.equals(other.param);
	}
	
	@Override
	public int hashCode() {
		return param.hashCode();
	}
}
