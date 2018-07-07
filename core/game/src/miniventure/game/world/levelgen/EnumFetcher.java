package miniventure.game.world.levelgen;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

class EnumFetcher<E extends Enum<E>> {
	
	private E[] types;
	private float[] ranges;
	
	@SuppressWarnings("unchecked")
	EnumFetcher(@NotNull Class<E> clazz, @NotNull String... typesWithOccurrences) {
		if(typesWithOccurrences.length == 0)
			throw new IllegalArgumentException("Must specify at least 1 type when creating a TileFetcher.");
		
		types = (E[]) Array.newInstance(clazz, typesWithOccurrences.length);
		ranges = new float[types.length];
		
		int totalCount = 0;
		int[] counts = new int[types.length];
		for(int i = 0; i < types.length; i++) {
			String tileData = typesWithOccurrences[i];
			types[i] = Enum.valueOf(clazz, tileData.substring(0, tileData.lastIndexOf("_")));
			counts[i] = Integer.parseInt(tileData.substring(tileData.lastIndexOf("_")+1));
			totalCount += counts[i];
		}
		
		int curCount = 0;
		for(int i = 0; i < counts.length; i++) {
			curCount += counts[i];
			ranges[i] = curCount * 1f / totalCount;
		}
	}
	
	public E getType(float value) {
		for(int i = 0; i < ranges.length; i++)
			if(value < ranges[i])
				return types[i];
		
		System.err.println("could not find type for value " + value + " from range end values " + Arrays.toString(ranges)+"; returning last type, " + types[types.length-1]);
		return types[types.length-1];
	}
}
