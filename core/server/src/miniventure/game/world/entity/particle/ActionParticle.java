package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.util.Version;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.ServerEntity;

import com.badlogic.gdx.utils.Array;

public class ActionParticle extends ServerEntity implements Particle {
	
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
	
	private final String spriteName;
	
	//private Animation<TextureRegion> animation;
	private final float animationTime;
	private float timeElapsed;
	
	private ActionParticle(String spriteName, float animationTime) {
		super();
		this.spriteName = spriteName;
		this.animationTime = animationTime;
		
		//Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		//animation = new Animation<>(animationTime / frames.size, frames);
		
		setRenderer(new AnimationRenderer(spriteName, animationTime, false));
	}
	
	protected ActionParticle(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		spriteName = data[0];
		animationTime = Float.parseFloat(data[1]);
		timeElapsed = Float.parseFloat(data[2]);
		
		//Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		//animation = new Animation<>(animationTime / frames.size, frames);
		
		setRenderer(new AnimationRenderer(spriteName, animationTime, false));
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			spriteName,
			animationTime+"",
			timeElapsed+""
		});
		
		return data;
	}
	
	@Override
	public void update(float delta) {
		timeElapsed += delta;
		
		if(timeElapsed >= animationTime)
			remove();
	}
	
	@Override
	public boolean isPermeableBy(Entity entity, boolean delegate) { return true; }
}
