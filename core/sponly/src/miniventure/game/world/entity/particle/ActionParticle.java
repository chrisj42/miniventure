package miniventure.game.world.entity.particle;

import miniventure.game.core.GdxCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.DirectionAnimationMap;
import miniventure.game.world.entity.EntitySpawn;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ActionParticle extends Particle {
	
	private final Animation<TextureHolder> animation;
	
	private ActionParticle(@NotNull EntitySpawn info, Animation<TextureHolder> animation) {
		super(info, animation.getAnimationDuration(), false);
		
		this.animation = animation;
	}
	
	@Override
	protected TextureHolder getSprite() {
		return animation.getKeyFrame(getAge());
	}
	
	public enum ActionType {
		SLASH(0.35f, true),
		PUNCH(0.3f, true),
		IMPACT(0.4f, false);
		
		public final float animationTime;
		private final boolean directional;
		private final Animation<TextureHolder> animation;
		private final DirectionAnimationMap animationMap;
		
		ActionType(float animationTime, boolean directional) {
			this.animationTime = animationTime;
			this.directional = directional;
			if(directional) {
				animation = null;
				animationMap = new DirectionAnimationMap(this::getSpriteName, animationTime, false, false);
			} else {
				animationMap = null;
				animation = getAnimation(getSpriteName(null));
			}
		}
		
		private String getSpriteName(Direction dir) {
			return "particle/"+name().toLowerCase()+(directional?'-'+dir.name().toLowerCase():"");
		}
		
		private Animation<TextureHolder> getAnimation(String name) {
			Array<TextureHolder> frames = GdxCore.entityAtlas.getRegions(name);
			return new Animation<>(frames.size / animationTime, frames, PlayMode.NORMAL);
		}
		
		public Animation<TextureHolder> getAnimation(Direction dir) {
			return directional ? animationMap.getAnimation(dir) : animation;
		}
		
		public ActionParticle makeParticle(@NotNull EntitySpawn entitySpawn, Direction dir) {
			return new ActionParticle(entitySpawn, getAnimation(dir));
		}
	}
}
