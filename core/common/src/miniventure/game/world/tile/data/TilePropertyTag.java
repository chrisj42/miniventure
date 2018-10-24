package miniventure.game.world.tile.data;

import miniventure.game.util.customenum.DataEnum;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.world.tile.SwimAnimation;

@SuppressWarnings("unchecked")
public class TilePropertyTag<T> extends DataEnum<T> {
	
	static {
		GenericEnum.registerEnum(TilePropertyTag.class, 4);
	}
	
	/* --- ENUMERATION VALUES --- */
	
	
	public static final TilePropertyTag<Float> LightRadius = new TilePropertyTag<>();
	public static final TilePropertyTag<Float> SpeedRatio = new TilePropertyTag<>();
	public static final TilePropertyTag<Float> ZOffset = new TilePropertyTag<>();
	public static final TilePropertyTag<SwimAnimation> Swim = new TilePropertyTag<>();
	
	
	public static TilePropertyTag<?> valueOf(String str) { return valueOf(TilePropertyTag.class, str); }
	
	private TilePropertyTag() {}
}
