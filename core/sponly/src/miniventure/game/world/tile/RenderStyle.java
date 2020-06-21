package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

public class RenderStyle {
	
	static final RenderStyle SINGLE_FRAME = new RenderStyle(PlayMode.NORMAL, 0);
	
	private final PlayMode playMode;
	private final float fps;
	
	private final boolean sync;
	
	RenderStyle(float fps) { this(false, fps); }
	RenderStyle(boolean sync, float fps) { this(PlayMode.LOOP, sync, fps); }
	RenderStyle(PlayMode playMode, float fps) { this(playMode, false, fps); }
	RenderStyle(PlayMode playMode, boolean sync, float fps) {
		this.playMode = playMode;
		this.sync = sync;
		this.fps = fps;
	}
	
	// public float getFPS() { return fps; }
	
	<T> TileAnimation getAnimation(T name, TileAnimationSetFrames<T> map) {
		return getAnimation(map.getFrames(name));
	}
	TileAnimation getAnimation(Array<TextureHolder> frames) {
		if(fps == 0)
			return new TileAnimation(true, 1, frames.get(0));
		else
			return new TileAnimation(sync, fps, frames, playMode);
	}
}
