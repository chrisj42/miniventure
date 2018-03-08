package miniventure.game.api;

public abstract class APIObject<T extends Enum<T> & APIObjectType<T, P>, P extends Property<P>> {
	
	public abstract String getData(Class<? extends P> property, T type, int propDataIndex);
	public abstract void setData(Class<? extends P> property, T type, int propDataIndex, String data);
	protected abstract T getType();
	
	protected int getIndex(T type, Class<? extends P> property, int propDataIndex) {
		if(!getType().equals(type))
			throw new IllegalArgumentException("APIObject " + this + " is not of the type " + type + ", cannot fetch the data index.");
		
		type.checkDataAccess(property, propDataIndex);
		
		return type.getPropDataIndex(property) + propDataIndex;
	}
	
	/*public String getData(Class<? extends P> property, T type, int propDataIndex) {
		return getDataArray()[getIndex(type, property, propDataIndex)];
	}*/
	
	/*public String[] getAllData(Class<? extends P> property, T type) {
		int idx = getIndex(type, property, 0);
		int len = type.getPropDataLength(property);
		String[] data = new String[len];
		System.arraycopy(getDataArray(), idx, data, 0, len);
		return data;
	}*/
	
}
