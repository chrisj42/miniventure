package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

public class TileAnimation extends Animation<TextureHolder> {
	
	private float startTime = -1;
	
	public TileAnimation(boolean sync, float frameDuration, Array<TextureHolder> keyFrames) {
		this(sync, frameDuration, keyFrames, PlayMode.LOOP);
	}
	
	public TileAnimation(boolean sync, float frameDuration, Array<TextureHolder> keyFrames, PlayMode playMode) {
		super(frameDuration, keyFrames, playMode);
		if(sync) startTime = 0;
	}
	
	public TileAnimation(boolean sync, float frameDuration, TextureHolder... keyFrames) {
		super(frameDuration, keyFrames);
		if(sync) startTime = 0;
	}
	
	boolean startedNonSync() { return startTime >= 0; }
	
	public TextureHolder getKeyFrame(Tile tile) {
		float time = tile.getWorld().getGameTime();
		if(startTime < 0)
			startTime = time;
		
		return super.getKeyFrame(time - startTime);
	}
}
