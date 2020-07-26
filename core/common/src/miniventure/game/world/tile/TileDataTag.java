package miniventure.game.world.tile;

import miniventure.game.util.customenum.DataEntry;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.customenum.SerialEnum;
import miniventure.game.util.customenum.SerialEnumMap;
import miniventure.game.util.customenum.Serializer;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.MapFunction;

public class TileDataTag<T> extends SerialEnum<T, TileDataTag<T>> {
	
	/* --- ENUMERATION VALUES --- */
	
	// Serializing
	
	public static final TileDataTag<Integer> Health =
		new TileDataTag<>(new Serializer<>(true, false, Integer.class));
	
	// Transition
	public static final TileDataTag<String> TransitionName =
		new TileDataTag<>(new Serializer<>(false, true, String.class));
	
	// Non-Serializing
	
	// Transition
	public static final TileDataTag<TransitionMode> TransitionMode = new TileDataTag<>();
	public static final TileDataTag<TileTypeInfo> TransitionTile = new TileDataTag<>();
	
	// if this one is present, then it is called during tile removal, after the exit animation
	public static final TileDataTag<Action> DestroyAction = new TileDataTag<>();
	
	// used for the current sprite, transition or otherwise.
	// only used for server, because on client rendering is more complex so animation data is stored differently
	public static final TileDataTag<Float> AnimationStart = new TileDataTag<>();
	
	// Update
	public static final TileDataTag<Float> LastUpdate = new TileDataTag<>();
	public static final TileDataTag<float[]> UpdateTimers = new TileDataTag<>();
	public static final TileDataTag<String[]> UpdateActionCaches = new TileDataTag<>();
	
	
	public static TileDataTag<?> valueOf(String str) { return valueOf(TileDataTag.class, str); }
	public static TileDataTag<?> valueOf(int ord) { return valueOf(TileDataTag.class, ord); }
	
	private TileDataTag() { super(null); }
	private TileDataTag(Serializer<T> serializer) { super(serializer); }
}
