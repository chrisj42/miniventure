package miniventure.game.world.tile;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.Point;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class TileAnimation extends Animation<TextureHolder> {
	
	// The reason this class isn't cached completely is essentially just for those tiles (namely water) that take advantage of random frame iteration.
	
	private final boolean sync;
	private final TileTypeEnum tileType;
	private final RenderTile tile;
	
	// private final boolean customSize;
	// private int offX, offY;
	
	/*TileAnimation(@NotNull TileTypeEnum tileType, boolean sync, float fps, Array<TextureHolder> keyFrames) {
		this(tileType, sync, fps, keyFrames, PlayMode.LOOP);
	}*/
	
	/*TileAnimation(@NotNull TileTypeEnum tileType, boolean sync, float fps, PlayMode playMode, Array<TextureHolder> keyFrames) {
		super(1f / fps, keyFrames, playMode);
		this.sync = sync;
		this.tileType = tileType;
	}*/
	
	TileAnimation(@NotNull RenderTile tile, @NotNull TileTypeEnum tileType, boolean sync, float fps, PlayMode playMode, TextureHolder... keyFrames) {
		super(1f / fps, keyFrames);
		setPlayMode(playMode);
		this.sync = sync;
		this.tileType = tileType;
		this.tile = tile;
		
		/*if(tileType.multi) {
			customSize = true;
			// a subsection of the tile texture needs to be drawn
			Point anchor = tile.getDataMap(tileType).get(TileDataTag.AnchorPos);
			offX = (tile.x - anchor.x) * Tile.SIZE;
			offY = (tile.y - anchor.y) * Tile.SIZE;
		} else
			customSize = false;*/
	}
	
	/*TextureHolder getKeyFrame() {
		float time = tile.getWorld().getGameTime();
		
		float startTime = 0;
		if(!sync) {
			// overlap sprites will almost always have no data map, and in fact would mess up the animation if they weren't global. So, we'll fetch the data map the back way.
			// TileDataMap dataMap = tile.getTypeStack().getCacheMap(tileType);
			// if(dataMap != null)
			startTime = tile.getLevel().animStartTimes.getOrDefaultAndPut(tile, time);
		}
		
		TextureHolder tex = super.getKeyFrame(time - startTime);
		
		if(tile.getWorld().getTileType(tileType).isMulti()) {
			Point anchor = tile.getDataMap(tileType).get(TileDataTag.AnchorPos);
			
		}
		
		return tex;
	}*/
	
	void drawSprite(SpriteBatch batch, float x, float y) {
		float time = tile.getWorld().getGameTime();
		
		float startTime = 0;
		if(!sync) {
			// overlap sprites will almost always have no data map, and in fact would mess up the animation if they weren't global. So, we'll fetch the data map the back way.
			// TileDataMap dataMap = tile.getTypeStack().getCacheMap(tileType);
			// if(dataMap != null)
			startTime = tile.getLevel().animStartTimes.getOrDefaultAndPut(tile, time);
		}
		
		TextureHolder tex = super.getKeyFrame(time - startTime);
		
		/*if(customSize)
			batch.draw(tex.texture.getTexture(), x, y, tex.texture.getRegionX() + offX, tex.texture.getRegionY() + offY, Tile.SIZE, Tile.SIZE);
		else
			*/batch.draw(tex.texture, x, y);
	}
}
