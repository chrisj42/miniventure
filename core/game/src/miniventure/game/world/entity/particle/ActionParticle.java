package miniventure.game.world.entity.particle;

import miniventure.game.GameCore;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ActionParticle extends Entity implements Particle {
	
	// may be animated; lasts as long as the animation, or lasts a given time, with a single frame.
	
	public enum ActionType {
		SLASH(0.35f, true),
		PUNCH(0.3f, true),
		IMPACT(0.4f, false);
		
		private final float animationTime;
		private final boolean directional;
		
		ActionType(float animationTime, boolean directional) {
			this.animationTime = animationTime;
			this.directional = directional;
		}
		
		public ActionParticle get(Direction dir) {
			return new ActionParticle("particle/"+name().toLowerCase()+(directional?"-"+dir.name().toLowerCase():""), animationTime);
		}
	}
	
	private Animation<TextureRegion> animation;
	private final float animationTime;
	private float timeElapsed;
	
	public ActionParticle(String spriteName, float animationTime) {
		super();
		this.animationTime = animationTime;
		Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		animation = new Animation<>(animationTime / frames.size, frames);
	}
	
	@Override
	protected TextureRegion getSprite() {
		return animation.getKeyFrame(timeElapsed, true);
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
