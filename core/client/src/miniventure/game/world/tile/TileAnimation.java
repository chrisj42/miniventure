package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.customenum.DataMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class TileAnimation extends Animation<TextureHolder> {
	
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
	
	TextureHolder getKeyFrame(Tile tile) {
		float time = tile.getWorld().getGameTime();
		
		float startTime = 0;
		if(!sync) {
			// overlap sprites will almost always have no data map, and in fact would mess up the animation if they weren't global. So, we'll fetch the data map the back way.
			DataMap dataMap = tile.getTypeStack().getCacheMap(tileType);
			if(dataMap != null)
				startTime = dataMap.putIfAbsent(TileCacheTag.AnimationStart, time);
		}
		
		return super.getKeyFrame(time - startTime);
	}
}
