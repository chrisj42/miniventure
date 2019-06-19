package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

class TileAnimation extends Animation<TextureHolder> {
	
	private float startTime = -1;
	
	TileAnimation(boolean sync, float fps, Array<TextureHolder> keyFrames) {
		this(sync, fps, keyFrames, PlayMode.LOOP);
	}
	
	TileAnimation(boolean sync, float fps, Array<TextureHolder> keyFrames, PlayMode playMode) {
		super(1f / fps, keyFrames, playMode);
		if(sync) startTime = 0;
	}
	
	TileAnimation(boolean sync, float fps, TextureHolder... keyFrames) {
		super(1f / fps, keyFrames);
		if(sync) startTime = 0;
	}
	
	TextureHolder getKeyFrame(Tile tile) {
		float time = tile.getWorld().getGameTime();
		if(startTime < 0)
			startTime = time;
		
		return super.getKeyFrame(time - startTime);
	}
}
