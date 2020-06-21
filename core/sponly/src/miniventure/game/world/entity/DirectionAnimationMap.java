package miniventure.game.world.entity;

import java.util.EnumMap;

import miniventure.game.core.GdxCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.function.MapFunction;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

public class DirectionAnimationMap {
	
	private final EnumMap<Direction, Animation<TextureHolder>> animations = new EnumMap<>(Direction.class);
	
	public DirectionAnimationMap(MapFunction<Direction, Animation<TextureHolder>> fetcher) {
		for (Direction dir : Direction.values)
			animations.put(dir, fetcher.get(dir));
	}
	
	public DirectionAnimationMap(MapFunction<Direction, String> nameFetcher, float duration, boolean isFrameDuration, boolean loopAnimation) {
		for (Direction dir : Direction.values) {
			Array<TextureHolder> frames = GdxCore.entityAtlas.getRegions(nameFetcher.get(dir));
			final float frameDur = isFrameDuration ? duration : frames.size / duration;
			animations.put(dir, new Animation<>(frameDur, frames, loopAnimation ? PlayMode.LOOP : PlayMode.NORMAL));
		}
	}
	
	public Animation<TextureHolder> getAnimation(Direction dir) {
		return animations.get(dir);
	}
}
