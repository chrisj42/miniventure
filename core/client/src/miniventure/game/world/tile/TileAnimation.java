package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class TileAnimation extends Animation<TextureHolder> {
	
	// The reason this class isn't cached completely is essentially just for those tiles (namely water) that take advantage of random frame iteration.
	
	private final boolean sync;
	private final TileTypeEnum tileType;
	
	TileAnimation(@NotNull TileTypeEnum tileType, boolean sync, float fps, Array<TextureHolder> keyFrames) {
		this(tileType, sync, fps, keyFrames, PlayMode.LOOP);
	}
	
	TileAnimation(@NotNull TileTypeEnum tileType, boolean sync, float fps, Array<TextureHolder> keyFrames, PlayMode playMode) {
		super(1f / fps, keyFrames, playMode);
		this.sync = sync;
		this.tileType = tileType;
	}
	
	TileAnimation(@NotNull TileTypeEnum tileType, boolean sync, float fps, TextureHolder... keyFrames) {
		super(1f / fps, keyFrames);
		this.sync = sync;
		this.tileType = tileType;
	}
	
	TextureHolder getKeyFrame(RenderTile tile) {
		float time = tile.getWorld().getGameTime();
		
		float startTime = 0;
		if(!sync) {
			// overlap sprites will almost always have no data map, and in fact would mess up the animation if they weren't global. So, we'll fetch the data map the back way.
			// TileDataMap dataMap = tile.getTypeStack().getCacheMap(tileType);
			// if(dataMap != null)
			startTime = tile.getLevel().animStartTimes.getOrDefaultAndPut(tile, time);
		}
		
		return super.getKeyFrame(time - startTime);
	}
}
