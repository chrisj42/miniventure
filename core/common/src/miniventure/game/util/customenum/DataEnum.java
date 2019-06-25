package miniventure.game.util.customenum;

public abstract class DataEnum<T> extends GenericEnum<DataEnum<T>> {
	
	protected DataEnum() {}
	
	public DataEntry<T> as(T value) { return new DataEntry<>(this, value); }
	
}
