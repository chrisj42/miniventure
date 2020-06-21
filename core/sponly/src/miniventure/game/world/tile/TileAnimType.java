package miniventure.game.world.tile;

import java.util.EnumMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;

public class TileAnimType<T> extends GenericEnum<T, TileAnimType<T>> {
	
	public static final TileAnimType<Integer> Main =
		new TileAnimType<Integer>("m", Integer::parseInt) {
		@Override
		public boolean tryRegister(TileType tileType, String spriteName, TextureHolder region) {
			if(spriteName.equals("main"))
				spriteName = "m00";
			return super.tryRegister(tileType, spriteName, region);
		}
	};
	
	public static final TileAnimType<Integer> Connection =
		new TileAnimType<>("c", Integer::parseInt);
	
	public static final TileAnimType<Integer> Overlap =
		new TileAnimType<>("o", Integer::parseInt);
	
	public static final TileAnimType<String> Transition =
		new TileAnimType<>("t", String::toString);
	
	
	private final String prefix;
	private final FetchFunction<TileAnimationSetFrames<T>> frameSetInstanceFetcher;
	private final EnumMap<TileType, TileAnimationSetFrames<T>> animationData = new EnumMap<>(TileType.class);
	
	private TileAnimType(String setPrefix, MapFunction<String, T> converter) {
		this(setPrefix, () -> TileAnimationSetFrames.from(converter));
	}
	private TileAnimType(String setPrefix, FetchFunction<TileAnimationSetFrames<T>> frameSetInstanceFetcher) {
		this.prefix = setPrefix;
		this.frameSetInstanceFetcher = frameSetInstanceFetcher;
	}
	
	public boolean tryRegister(TileType tileType, String spriteName, TextureHolder region) {
		if(!spriteName.startsWith(prefix)) return false;
		animationData
			.computeIfAbsent(tileType, k -> frameSetInstanceFetcher.get())
			.addFrame(spriteName.substring(prefix.length()), region);
		return true;
	}
	
	public TileAnimationSetFrames<T> fetchMap(TileType tileType) {
		return animationData.computeIfAbsent(tileType, k -> frameSetInstanceFetcher.get());
	}
	
	public boolean hasSprites(TileType tileType) {
		return fetchMap(tileType).getAnimationCount() > 0;
	}
}
