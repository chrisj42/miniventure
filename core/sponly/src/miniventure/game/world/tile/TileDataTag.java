package miniventure.game.world.tile;

import miniventure.game.util.customenum.DataEntry;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.TileType.TileTypeEnum;

public class TileDataTag<T> extends SerialEnum<T, TileDataTag<T>> {
	
	/* --- ENUMERATION VALUES --- */
	
	public static final TileDataTag<Integer> Health =
		new TileDataTag<>(true, true, false, Integer.class);
	
	// Transition
	public static final TileDataTag<String> TransitionName =
		new TileDataTag<>(true, false, true, String.class);
	
	// below are cache values that are neither saved nor sent
	
	// Transition
	public static final TileDataTag<TransitionMode> TransitionMode = new TileDataTag<>(true);
	public static final TileDataTag<TileTypeEnum> TransitionTile = new TileDataTag<>(true);
	
	// if this one is present, then it is called during tile removal, after the exit animation
	// public static final TileDataTag<Action> DestroyAction = new TileDataTag<>();
	
	// used for the current sprite, transition or otherwise.
	// only needed for transitions, and non-global tile animations (that flow nicely from their transition)
	// all others use a global start time
	public static final TileDataTag<Float> AnimationStart = new TileDataTag<>(true);
	
	// Update
	public static final TileDataTag<Float> UpdateTimer = new TileDataTag<>(true);
	public static final TileDataTag<Float> LastUpdate = new TileDataTag<>(false);
	// public static final TileDataTag<float[]> UpdateTimers = new TileDataTag<>(false);
	// public static final TileDataTag<String[]> UpdateActionCaches = new TileDataTag<>(false);
	
	public static final TileDataTag<?>[] values = GenericEnum.values(TileDataTag.class);
	
	public static TileDataTag<?> valueOf(String str) { return valueOf(TileDataTag.class, str); }
	public static TileDataTag<?> valueOf(int ord) { return valueOf(TileDataTag.class, ord); }
	
	
	public final boolean perLayer;
	
	private TileDataTag(boolean perLayer) {
		super();
		this.perLayer = perLayer;
	}
	private TileDataTag(boolean perLayer, boolean save, boolean send, MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(save, send, valueWriter, valueParser);
		this.perLayer = perLayer;
	}
	
	// private TileDataTag(Class<T> valueClass) { this(false, false, valueClass); }
	private TileDataTag(boolean perLayer, boolean save, boolean send, Class<T> valueClass) {
		super(save, send, valueClass);
		this.perLayer = perLayer;
	}
	
	/** @noinspection rawtypes*/
	@SuppressWarnings("unchecked")
	public static class TileTypeDataMap extends SerialEnumMap<TileDataTag> {
		
		public TileTypeDataMap() {
			super(TileDataTag.class);
		}
		
		public TileTypeDataMap(DataEntry<?, ? extends TileDataTag>... entries) {
			super(TileDataTag.class, entries);
		}
		
		public TileTypeDataMap(String alldata) {
			super(alldata, TileDataTag.class);
		}
	}
}
