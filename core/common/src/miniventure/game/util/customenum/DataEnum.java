package miniventure.game.util.customenum;

@SuppressWarnings("unchecked")
public abstract class DataEnum<T> extends GenericEnum<DataEnum<T>> {
	
	protected DataEnum() {}
	
	public DataEntry<T> as(T value) { return new DataEntry<>(this, value); }
	
	/** @noinspection NoopMethodInAbstractClass*/
	public static void init() {}
}
