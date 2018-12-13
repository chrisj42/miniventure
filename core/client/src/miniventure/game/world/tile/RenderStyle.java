package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class RenderStyle {
	
	static final RenderStyle SINGLE_FRAME = new RenderStyle(PlayMode.NORMAL, 0);
	
	final PlayMode playMode;
	final float time;
	private final boolean isFrameTime;
	
	final boolean sync;
	
	RenderStyle(float frameTime) { this(false, frameTime, true); }
	RenderStyle(boolean sync, float frameTime) { this(sync, frameTime, true); }
	RenderStyle(float time, boolean isFrameTime) { this(PlayMode.LOOP, time, isFrameTime); }
	RenderStyle(boolean sync, float time, boolean isFrameTime) { this(PlayMode.LOOP, sync, time, isFrameTime); }
	RenderStyle(PlayMode playMode, float frameTime) { this(playMode, false, frameTime, true); }
	RenderStyle(PlayMode playMode, boolean sync, float frameTime) { this(playMode, sync, frameTime, true); }
	RenderStyle(PlayMode playMode, float time, boolean isFrameTime) { this(playMode, false, time, isFrameTime); }
	RenderStyle(PlayMode playMode, boolean sync, float time, boolean isFrameTime) {
		this.playMode = playMode;
		this.sync = sync;
		this.time = time;
		this.isFrameTime = isFrameTime;
	}
	
	TileAnimation<TextureHolder> getAnimation(@NotNull TileTypeEnum tileType, String name, EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> map) {
		if(!map.containsKey(tileType))
			throw new SpriteNotFoundException("tile type "+tileType+" does not have any registered overlap sprites. (discovered when attempting to fetch sprite \""+name+"\")");
		if(!map.get(tileType).containsKey(name))
			throw new SpriteNotFoundException("tile type "+tileType+" does not overlap sprites with name \""+name+"\".");
		return getAnimation(tileType, map.get(tileType).get(name));
	}
	TileAnimation<TextureHolder> getAnimation(@NotNull TileTypeEnum tileType, Array<TextureHolder> frames) {
		if(time == 0)
			return new TileAnimation<>(sync, 1, frames.get(0));
		else
			return new TileAnimation<>(sync, isFrameTime ? time : time / frames.size, frames, playMode);
	}
	
	// TODO this ought to be a checked exception
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
}
