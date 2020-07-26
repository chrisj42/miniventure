package miniventure.game.world.tile;

import java.util.Arrays;

import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;

class GEnumOrderedDataSet<T extends GenericEnum> {
	
	private static final Object[] NO_DATA = new Object[0];
	
	private final int[] dataPositions;
	private final int[] typeByPos;
	private int nextDataPos;
	
	private final Class<T> clazz;
	
	GEnumOrderedDataSet(Class<T> clazz) {
		dataPositions = new int[GenericEnum.values(clazz).length];
		typeByPos = new int[GenericEnum.values(clazz).length];
		this.clazz = clazz;
		Arrays.fill(dataPositions, -1);
		Arrays.fill(typeByPos, -1);
		nextDataPos = 0;
	}
	
	public void addKey(T key) {
		typeByPos[nextDataPos] = key.ordinal();
		dataPositions[key.ordinal()] = nextDataPos++;
	}
	
	public int getDataSize() { return nextDataPos; }
	
	public Object[] createDataArray() {
		return getDataSize() == 0 ? NO_DATA : new Object[getDataSize()];
	}
	
	public int getDataIndex(T key) {
		return dataPositions[key.ordinal()];
	}
	
	public T getTypeAt(int index) { return GenericEnum.valueOf(clazz, typeByPos[index]); }
	
}
