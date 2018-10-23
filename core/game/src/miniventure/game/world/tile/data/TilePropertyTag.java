package miniventure.game.world.tile.data;

import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.tile.SwimAnimation;

@SuppressWarnings("unchecked")
public class TilePropertyTag<T> extends SerialEnum<T> {
	
	static {
		GenericEnum.registerEnum(TilePropertyTag.class, 4);
	}
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final TilePropertyTag<Float> LightRadius = new TilePropertyTag<>(Float.class);
	public static final TilePropertyTag<Float> SpeedRatio = new TilePropertyTag<>(Float.class);
	public static final TilePropertyTag<Float> ZOffset = new TilePropertyTag<>(Float.class);
	public static final TilePropertyTag<SwimAnimation> Swim = new TilePropertyTag<>(SwimAnimation::serialize, SwimAnimation::deserialize);
	
	
	public static TilePropertyTag<?> valueOf(String str) { return valueOf(TilePropertyTag.class, str); }
	
	private TilePropertyTag(MapFunction<T, String> valueWriter, MapFunction<String, T> valueParser) {
		super(valueWriter, valueParser);
	}
	
	private TilePropertyTag(Class<T> valueClass) {
		super(valueClass);
	}
}
