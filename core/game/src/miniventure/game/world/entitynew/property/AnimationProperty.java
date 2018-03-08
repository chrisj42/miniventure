package miniventure.game.world.entitynew.property;

import miniventure.game.GameCore;
import miniventure.game.world.entitynew.DataCarrier;
import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.InstanceData;
import miniventure.game.world.entitynew.InstanceData.FloatValue;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationProperty extends RenderProperty implements DataCarrier<FloatValue> {
	
	private final Animation<TextureRegion> animation;
	
	public AnimationProperty(String spriteName, float frameTime) {
		animation = new Animation<>(frameTime, GameCore.entityAtlas.findRegions(spriteName));
	}
	
	@Override
	public void render(Entity e, float delta, SpriteBatch batch, float x, float y) {
		FloatValue animationTime = e.getDataObject(AnimationProperty.class, getUniquePropertyClass());
		animationTime.value += delta;
		
		batch.draw(animation.getKeyFrame(animationTime.value, true), x, y);
	}
	
	@Override
	public InstanceData getInitialDataObject() {
		return new FloatValue();
	}
	
	@Override
	public String[] getInitialData() { return new String[] {"0"}; }
}
