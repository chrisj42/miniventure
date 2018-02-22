package miniventure.game.world.entity;

import miniventure.game.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class ActionParticle extends Entity {
	
	// may be animated; lasts as long as the animation, or lasts a given time, with a single frame.
	
	private Animation<TextureRegion> animation;
	private final float animationTime;
	private float timeElapsed;
	
	public ActionParticle(String spriteName, float animationTime) {
		super(new TextureRegion());
		this.animationTime = animationTime;
		Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		animation = new Animation<>(animationTime / frames.size, frames);
	}
	
	@Override
	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.setSize(1);
		return bounds;
	}
	
	@Override
	public void drawSprite(SpriteBatch batch, float x, float y) {
		batch.draw(animation.getKeyFrame(timeElapsed, true), x, y);
	}
	
	@Override
	public void update(float delta) {
		timeElapsed += Gdx.graphics.getDeltaTime();
		
		if(timeElapsed >= animationTime)
			remove();
	}
	
	@Override
	public boolean isPermeableBy(Entity entity, boolean delegate) { return true; }
}
