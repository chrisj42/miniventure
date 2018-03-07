package miniventure.game.api;

public abstract class APIObject<T extends Enum<T> & APIObjectType<T, P>, P extends Property<P>> {
	
	
	protected abstract String[] getDataArray();
	protected abstract T getType();
	
	protected int getIndex(T type, Class<? extends P> property, int propDataIndex) {
		if(!getType().equals(type))
			throw new IllegalArgumentException("APIObject " + this + " is not of the type " + type + ", cannot fetch the data index.");
		
		type.checkDataAccess(property, propDataIndex);
		
		return type.getPropDataIndex(property) + propDataIndex;
	}
	
	public String getData(Class<? extends P> property, T type, int propDataIndex) {
		return getDataArray()[getIndex(type, property, propDataIndex)];
	}
	
	public void setData(Class<? extends P> property, T type, int propDataIndex, String data) {
		getDataArray()[getIndex(type, property, propDataIndex)] = data;
	}
	
}
