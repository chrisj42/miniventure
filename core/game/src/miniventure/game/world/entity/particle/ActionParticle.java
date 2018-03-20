package miniventure.game.world.entity.particle;

import java.util.Arrays;

import miniventure.game.GameCore;
import miniventure.game.util.Version;
import miniventure.game.world.WorldManager;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

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
		
		public ActionParticle get(@NotNull WorldManager world, Direction dir) {
			return new ActionParticle(world, "particle/"+name().toLowerCase()+(directional?"-"+dir.name().toLowerCase():""), animationTime);
		}
	}
	
	private final String spriteName;
	
	private Animation<TextureRegion> animation;
	private final float animationTime;
	private float timeElapsed;
	
	private ActionParticle(@NotNull WorldManager world, String spriteName, float animationTime) {
		super(world);
		this.spriteName = spriteName;
		this.animationTime = animationTime;
		Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		animation = new Animation<>(animationTime / frames.size, frames);
	}
	
	protected ActionParticle(@NotNull WorldManager world, String[][] allData, Version version) {
		super(world, Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		spriteName = data[0];
		animationTime = Float.parseFloat(data[1]);
		timeElapsed = Float.parseFloat(data[2]);
		
		Array<AtlasRegion> frames = GameCore.entityAtlas.findRegions(spriteName);
		animation = new Animation<>(animationTime / frames.size, frames);
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
