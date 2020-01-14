package miniventure.game.world.tile;

import miniventure.game.util.customenum.DataEnum;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.function.Action;

public class TileCacheTag<T> extends DataEnum<T> {
	
	static {
		GenericEnum.registerEnum(TileCacheTag.class, 7);
	}
	
	public static void init() {}
	
	/* --- ENUMERATION VALUES --- */
	
	
	// Transition
	public static final TileCacheTag<TransitionMode> TransitionMode = new TileCacheTag<>();
	public static final TileCacheTag<TileTypeInfo> TransitionTile = new TileCacheTag<>();
	
	// if this one is present, then it is called during tile removal, after the exit animation
	public static final TileCacheTag<Action> DestroyAction = new TileCacheTag<>();
	
	// used for the current sprite, transition or otherwise.
	public static final TileCacheTag<Float> AnimationStart = new TileCacheTag<>();
	
	// Update
	public static final TileCacheTag<Float> LastUpdate = new TileCacheTag<>();
	public static final TileCacheTag<float[]> UpdateTimers = new TileCacheTag<>();
	public static final TileCacheTag<String[]> UpdateActionCaches = new TileCacheTag<>();
	
	
	public static TileCacheTag<?> valueOf(String str) { return valueOf(TileCacheTag.class, str); }
	public static TileCacheTag<?> valueOf(int ord) { return valueOf(TileCacheTag.class, ord); }
	
	
	/*private TileCacheTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this(false, false, valueWriter, valueParser);
	}
	private TileCacheTag(boolean save, boolean send, MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(save, send, valueWriter, valueParser);
	}
	
	private TileCacheTag() { this(false, false, valueClass); }
	private TileCacheTag(boolean save, boolean send, Class<T> valueClass) {
		super(save, send, valueClass);
	}*/
	
}
