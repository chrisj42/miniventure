package miniventure.game.world.tile;

import miniventure.game.world.WorldManager;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TileAnimation<T> extends Animation<T> {
	
	private float startTime = -1;
	
	public TileAnimation(boolean sync, float frameDuration, Array<? extends T> keyFrames) {
		this(sync, frameDuration, keyFrames, PlayMode.LOOP);
	}
	
	public TileAnimation(boolean sync, float frameDuration, Array<? extends T> keyFrames, PlayMode playMode) {
		super(frameDuration, keyFrames, playMode);
		if(sync) startTime = 0;
	}
	
	@SafeVarargs
	public TileAnimation(boolean sync, float frameDuration, T... keyFrames) {
		super(frameDuration, keyFrames);
		if(sync) startTime = 0;
	}
	
	public T getKeyFrame(@NotNull WorldManager world) {
		float time = world.getGameTime();
		if(startTime < 0)
			startTime = time;
		
		return super.getKeyFrame(time - startTime);
	}
}
