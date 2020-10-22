package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class RenderStyle {
	
	static final RenderStyle SINGLE_FRAME = new RenderStyle(PlayMode.NORMAL, 0);
	
	private final PlayMode playMode;
	private final float fps;
	
	private final boolean sync;
	
	RenderStyle(float fps) { this(fps == 0, fps); }
	RenderStyle(boolean sync, float fps) { this(PlayMode.LOOP, sync, fps); }
	RenderStyle(PlayMode playMode, float fps) { this(playMode, fps == 0, fps); }
	RenderStyle(PlayMode playMode, boolean sync, float fps) {
		this.playMode = playMode;
		this.sync = sync;
		this.fps = fps;
	}
	
	boolean isSync() { return sync; }
	
	<T> TileAnimation getAnimation(@NotNull RenderTile tile, @NotNull TileTypeEnum tileType, T name, TileTypeToAnimationMap<T> map, String mapNameForErrorLog) {
		return getAnimation(tile, tileType, map.getAnimationFrames(tileType, name, mapNameForErrorLog));
	}
	private TileAnimation getAnimation(@NotNull RenderTile tile, @NotNull TileTypeEnum tileType, Array<TextureHolder> frames) {
		if(fps == 0)
			return new TileAnimation(tile, tileType, sync, 1, playMode, frames.get(0));
		else
			return new TileAnimation(tile, tileType, sync, fps, playMode, frames.items);
	}
}
