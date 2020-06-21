package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.Tile.TileContext;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

class TileAnimation extends Animation<TextureHolder> {
	
	private final boolean sync;
	
	TileAnimation(boolean sync, float fps, Array<TextureHolder> keyFrames) {
		this(sync, fps, keyFrames, PlayMode.LOOP);
	}
	
	TileAnimation(boolean sync, float fps, Array<TextureHolder> keyFrames, PlayMode playMode) {
		super(1f / fps, keyFrames, playMode);
		this.sync = sync;
	}
	
	TileAnimation(boolean sync, float fps, TextureHolder... keyFrames) {
		super(1f / fps, keyFrames);
		this.sync = sync;
	}
	
	TextureHolder getKeyFrame(TileContext context) {
		if(getKeyFrames().length < 2)
			return super.getKeyFrame(0);
		
		float time = context.getWorld().getGameTime();
		
		float startTime = 0;
		if(!sync) {
			// if there are multiple animations on this layer of the current tile, then AnimationStart will be shared among them, since this tile data is stored by layer.
			startTime = context.getOrInitData(TileDataTag.AnimationStart, time);
		}
		
		return super.getKeyFrame(time - startTime);
	}
}
