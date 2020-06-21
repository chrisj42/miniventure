package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.function.MapFunction;

import com.badlogic.gdx.utils.Array;

abstract class TileAnimationSetFrames<T> {
	
	private final HashMap<T, Array<TextureHolder>> animationMap = new HashMap<>();
	
	TileAnimationSetFrames() {}
	
	public int getAnimationCount() { return animationMap.size(); }
	
	public void addFrame(String animationId, TextureHolder region) {
		T id = fromString(animationId);
		animationMap.computeIfAbsent(id, k -> new Array<>(TextureHolder.class)).add(region);
	}
	
	Array<TextureHolder> getFrames(T id) {
		return animationMap.get(id);
	}
	
	HashMap<T, TileAnimation> compileAnimations(AnimationSetCompiler<T> compiler) {
		HashMap<T, TileAnimation> animations = new HashMap<>(animationMap.size());
		
		animationMap.forEach((id, frames) -> animations.put(id, compiler.getStyle(id).getAnimation(frames)));
		
		return animations;
	}
	
	abstract T fromString(String name);
	
	public static <T> TileAnimationSetFrames<T> from(MapFunction<String, T> converter) {
		return new TileAnimationSetFrames<T>() {
			@Override
			T fromString(String name) {
				return converter.get(name);
			}
		};
	}
}
