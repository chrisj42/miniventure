package miniventure.game.world.tile;

import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;

public class TileCacheTag<T> extends SerialEnum<T> {
	
	static {
		GenericEnum.registerEnum(TileCacheTag.class, 8);
	}
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final TileCacheTag<Integer> Health =
		new TileCacheTag<>(true, false, Integer.class);
	
	// Transition
	public static final TileCacheTag<String> TransitionName =
		new TileCacheTag<>(false, true, String.class);
	
	public static final TileCacheTag<Float> TransitionStart = new TileCacheTag<>(Float.class);
	public static final TileCacheTag<TransitionMode> TransitionMode = new TileCacheTag<>(TransitionMode.class);
	public static final TileCacheTag<TileTypeEnum> TransitionTile = new TileCacheTag<>(TileTypeEnum.class);
	
	// Update
	public static final TileCacheTag<Float> LastUpdate = new TileCacheTag<>(Float.class);
	public static final TileCacheTag<float[]> UpdateTimers = new TileCacheTag<>(float[].class);
	public static final TileCacheTag<String[]> UpdateActionCaches = new TileCacheTag<>(String[].class);
	
	
	public static TileCacheTag<?> valueOf(String str) { return valueOf(TileCacheTag.class, str); }
	public static TileCacheTag<?> valueOf(int ord) { return valueOf(TileCacheTag.class, ord); }
	
	
	private TileCacheTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this(false, false, valueWriter, valueParser);
	}
	private TileCacheTag(boolean save, boolean send, MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(save, send, valueWriter, valueParser);
	}
	
	private TileCacheTag(Class<T> valueClass) { this(false, false, valueClass); }
	private TileCacheTag(boolean save, boolean send, Class<T> valueClass) {
		super(save, send, valueClass);
	}
	
}
