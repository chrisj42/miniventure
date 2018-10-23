package miniventure.game.world.tile.data;

import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.TransitionManager.TransitionMode;

@SuppressWarnings("unchecked")
public class TileCacheTag<T> extends SerialEnum<T> {
	
	static {
		GenericEnum.registerEnum(TileCacheTag.class, 8);
	}
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final TileCacheTag<Integer> Health = new TileCacheTag<>(Integer.class);
	
	// Rendering
	public static final TileCacheTag<String> TransitionName = new TileCacheTag<>(String.class);
	public static final TileCacheTag<Float> TransitionStart = new TileCacheTag<>(Float.class);
	public static final TileCacheTag<TransitionMode> TransitionMode = new TileCacheTag<>(TransitionMode.class);
	public static final TileCacheTag<TileTypeEnum> TransitionTile = new TileCacheTag<>(TileTypeEnum.class);
	
	// Update
	public static final TileCacheTag<Float> LastUpdate = new TileCacheTag<>(Float.class);
	public static final TileCacheTag<float[]> UpdateTimers = new TileCacheTag<>(float[].class);
	public static final TileCacheTag<String[]> UpdateActionCaches = new TileCacheTag<>(String[].class);
	
	
	public static TileCacheTag<?> valueOf(String str) { return valueOf(TileCacheTag.class, str); }
	
	private TileCacheTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(valueWriter, valueParser);
	}
	
	private TileCacheTag(Class<T> valueClass) {
		super(valueClass);
	}
	
}
