package miniventure.game.api;

public interface APIObject<T extends Enum<T> & APIObject<T, P>, P extends Property<P>> {
	
	P[] getProperties();
	
	Class<T> getTypeClass();
	T getInstance();
	
	default TypeInstance<T, P> getTypeInstance() {
		return TypeLoader.getType(getTypeClass()).getTypeInstance(getInstance());
	}
	
	default <Q extends P> Q getProp(Class<Q> clazz) { return getTypeInstance().getProp(clazz); }
	
	default int getDataLength() { return getTypeInstance().getDataLength(); }
	default String[] getInitialData() { return getTypeInstance().getInitialData(); }
	
	default void checkDataAccess(Class<? extends P> property, int propDataIndex) {
		getTypeInstance().checkDataAccess(property, propDataIndex);
	}
	
	default int getPropDataIndex(Class<? extends P> prop) { return getTypeInstance().getPropDataIndex(prop); }
	default int getPropDataLength(Class<? extends P> prop) { return getTypeInstance().getPropDataLength(prop); }
	
}
