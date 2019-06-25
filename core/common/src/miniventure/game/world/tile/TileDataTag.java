package miniventure.game.world.tile;

import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;

public class TileDataTag<T> extends SerialEnum<T> {
	
	static {
		GenericEnum.registerEnum(TileDataTag.class, 2);
	}
	
	public static void init() {}
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final TileDataTag<Integer> Health =
		new TileDataTag<>(true, false, Integer.class);
	
	// Transition
	public static final TileDataTag<String> TransitionName =
		new TileDataTag<>(false, true, String.class);
	
	
	
	public static TileDataTag<?> valueOf(String str) { return valueOf(TileDataTag.class, str); }
	public static TileDataTag<?> valueOf(int ord) { return valueOf(TileDataTag.class, ord); }
	
	
	private TileDataTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		this(false, false, valueWriter, valueParser);
	}
	private TileDataTag(boolean save, boolean send, MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(save, send, valueWriter, valueParser);
	}
	
	// private TileDataTag(Class<T> valueClass) { this(false, false, valueClass); }
	private TileDataTag(boolean save, boolean send, Class<T> valueClass) {
		super(save, send, valueClass);
	}
	
}
