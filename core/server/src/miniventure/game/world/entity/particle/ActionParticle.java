package miniventure.game.world.entity.particle;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.world.entity.ClassDataList;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.ServerEntity;

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
		
		setRenderer(new AnimationRenderer(spriteName, animationTime, false, false));
	}
	
	protected ActionParticle(ClassDataList allData, final Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
		ArrayList<String> data = allData.get(1);
		spriteName = data.get(0);
		animationTime = Float.parseFloat(data.get(1));
		timeElapsed = Float.parseFloat(data.get(2));
		
		//Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		//animation = new Animation<>(animationTime / frames.size, frames);
		
		setRenderer(new AnimationRenderer(spriteName, animationTime, false, false));
	}
	
	@Override
	public ClassDataList save() {
		ClassDataList allData = super.save();
		ArrayList<String> data = new ArrayList<>(Arrays.asList(
			spriteName,
			String.valueOf(animationTime),
			String.valueOf(timeElapsed)
		));
		
		allData.add(data);
		return allData;
	}
	
	@Override
	public void update(float delta) {
		timeElapsed += delta;
		
		if(timeElapsed >= animationTime)
			remove();
	}
	
	@Override
	public boolean isPermeable() { return true; }
}
