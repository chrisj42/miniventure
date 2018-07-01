package miniventure.game.world.tile.newtile.render;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

public class RenderStyle {
	
	public static final RenderStyle SINGLE_FRAME = new RenderStyle(PlayMode.NORMAL, 0);
	
	private final PlayMode playMode;
	final float time;
	private final boolean isFrameTime;
	
	public RenderStyle(float frameTime) { this(frameTime, true); }
	public RenderStyle(float time, boolean isFrameTime) { this(PlayMode.LOOP, time, isFrameTime); }
	public RenderStyle(PlayMode playMode, float frameTime) { this(playMode, frameTime, true); }
	public RenderStyle(PlayMode playMode, float time, boolean isFrameTime) {
		this.playMode = playMode;
		this.time = time;
		this.isFrameTime = isFrameTime;
	}
	
	public Animation<TextureHolder> getAnimation(Array<TextureHolder> frames) {
		if(time == 0)
			return new Animation<>(1, frames.get(0));
		
		return new Animation<>(isFrameTime ? time : time/frames.size, frames, playMode);
	}
}
