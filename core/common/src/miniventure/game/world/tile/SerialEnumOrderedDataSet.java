package miniventure.game.world.tile;

import miniventure.game.util.customenum.SerialEnum;

class SerialEnumOrderedDataSet<T extends SerialEnum> extends GEnumOrderedDataSet<T> {
	
	private final GEnumOrderedDataSet<T> save;
	private final GEnumOrderedDataSet<T> send;
	
	SerialEnumOrderedDataSet(Class<T> clazz) {
		super(clazz);
		save = new GEnumOrderedDataSet<>(clazz);
		send = new GEnumOrderedDataSet<>(clazz);
	}
	
	@Override
	public void addKey(T key) {
		super.addKey(key);
		if(key.savable())
			save.addKey(key);
		if(key.sendable())
			send.addKey(key);
	}
	
	public int getDataSize(boolean save) {
		return save ? this.save.getDataSize() : send.getDataSize();
	}
	
	public int getDataIndex(T key, boolean save) {
		return save ? this.save.getDataIndex(key) : send.getDataIndex(key);
	}
	
	public T getTypeAt(int index, boolean save) {
		return save ? this.save.getTypeAt(index) : send.getTypeAt(index);
	}
	
}
