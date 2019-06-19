package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

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
	
	TileAnimation getAnimation(@NotNull TileTypeEnum tileType, String name, EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> map, String mapNameForErrorLog) {
		HashMap<String, Array<TextureHolder>> tileAnims = map.get(tileType);
		if(tileAnims == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have any registered sprites in map "+mapNameForErrorLog+". (discovered when attempting to fetch sprite \""+name+"\")");
		
		Array<TextureHolder> anim = tileAnims.get(name);
		if(anim == null)
			throw new SpriteNotFoundException("tile type "+tileType+" does not have sprite with name \""+name+"\" in map "+mapNameForErrorLog+'.');
		
		return getAnimation(anim);
	}
	private TileAnimation getAnimation(Array<TextureHolder> frames) {
		if(fps == 0)
			return new TileAnimation(sync, 1, frames.get(0));
		else
			return new TileAnimation(sync, fps, frames, playMode);
	}
	
	// TODO this ought to be a checked exception
	private static class SpriteNotFoundException extends RuntimeException {
		SpriteNotFoundException(String msg) {
			super(msg);
		}
	}
}
