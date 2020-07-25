package miniventure.game.world.tile;

import miniventure.game.util.customenum.DataEntry;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.util.customenum.Serializer;
import miniventure.game.util.function.MapFunction;

public class TileDataTag<T> extends SerialEnum<T, TileDataTag<T>> {
	
	/* --- ENUMERATION VALUES --- */
	
	public static final TileDataTag<Integer> Health =
		new TileDataTag<>(new Serializer<>(true, false, Integer.class));
	
	// Transition
	public static final TileDataTag<String> TransitionName =
		new TileDataTag<>(new Serializer<>(false, true, String.class));
	
	
	
	public static TileDataTag<?> valueOf(String str) { return valueOf(TileDataTag.class, str); }
	public static TileDataTag<?> valueOf(int ord) { return valueOf(TileDataTag.class, ord); }
	
	
	private TileDataTag(Serializer<T> serializer) {
		super(serializer);
	}
	
	/** @noinspection rawtypes*/
	@SuppressWarnings("unchecked")
	public static class TileDataEnumMap extends SerialEnumMap<TileDataTag> {
		
		public TileDataEnumMap() {
			super();
		}
		
		public TileDataEnumMap(DataEntry<?, ? extends TileDataTag>... entries) {
			super(entries);
		}
		
		public TileDataEnumMap(SerialEnumMap<TileDataTag> model) {
			super(model);
		}
		
		public TileDataEnumMap(String alldata) {
			super(alldata, TileDataTag.class);
		}
	}
}
