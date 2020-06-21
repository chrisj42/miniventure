package miniventure.game.world.entity;

import miniventure.game.texture.TextureHolder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

public class EntityAnimation extends Animation<TextureHolder> {
	public EntityAnimation(float frameDuration, Array<? extends TextureHolder> keyFrames) {
		super(frameDuration, keyFrames);
	}
	
	public EntityAnimation(float frameDuration, Array<? extends TextureHolder> keyFrames, PlayMode playMode) {
		super(frameDuration, keyFrames, playMode);
	}
	
	public EntityAnimation(float frameDuration, TextureHolder... keyFrames) {
		super(frameDuration, keyFrames);
	}
}
