package miniventure.game.world.entity.mob;

import java.util.HashMap;
import java.util.PriorityQueue;

import miniventure.game.GameCore;
import miniventure.game.world.entity.Direction;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobAnimationController {
	
	private static final HashMap<Class<? extends Mob>, MobAnimation> mobAnimations = new HashMap<>();
	
	private static class MobAnimation {
		private final HashMap<String, Animation<TextureRegion>> animations;
		
		public MobAnimation(String mobSpriteName) {
			animations = new HashMap<>();
			
			for(AnimationState state: AnimationState.values) {
				for(String dir: Direction.names) {
					String name = state.name().toLowerCase()+"-"+dir;
					animations.put(name, new Animation<>(state.frameDuration, GameCore.entityAtlas.findRegions(mobSpriteName+"/"+name)));
				}
			}
		}
	}
	
	public enum AnimationState {
		/* this technically will be just for some more advanced functionality, but it is all built-in to the enum class.
			Enum implements Comparable, and compares based on the ordinal, I imagine.
			I will use the order of the enums to specify the "priority" of the animation state.
		*/
		BLOCK(1.0f), ATTACK(0.15f), RUN(0.1f), WALK(0.2f), IDLE(1.0f);
		
		public final float frameDuration;
		
		AnimationState(float frameDuration) { this.frameDuration = frameDuration; }
		
		public static final AnimationState[] values = values();
	}
	
	private Mob mob;
	//private TextureAtlas atlas; // TODO this needs to be redone so that there is a single, static animator instance of each type of mob, so that they are not continuously created, which is not efficient.
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState, state;
	
	private float animationTime;
	private HashMap<String, Animation<TextureRegion>> animations;
	
	private PriorityQueue<AnimationState> requestedAnimations;
	
	public MobAnimationController(Mob mob, String spriteName) {
		state = AnimationState.IDLE;
		this.mob = mob;
		
		requestedAnimations = new PriorityQueue<>();
		
		Class<? extends Mob> mobClass = mob.getClass();
		
		if(!mobAnimations.containsKey(mobClass))
			mobAnimations.put(mobClass, new MobAnimation(spriteName));
		
		animations = mobAnimations.get(mobClass).animations;
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	void requestState(AnimationState rState) { requestedAnimations.add(rState); }
	
	TextureRegion pollAnimation(float delta) {
		// update the animation
		animationTime += delta;
		// fetch the current frame
		String textureName = state.name().toLowerCase() + "-" + mob.getDirection().name().toLowerCase();
		Animation<TextureRegion> ani = animations.get(textureName);
		if(animationTime > ani.getAnimationDuration()) animationTime = 0; // reset the animationTime to prevent any possibility of overflow
		TextureRegion frame = ani.getKeyFrame(animationTime, true);
		
		// reset the animation
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		if(state != prevState)
			animationTime = 0; // if we're going to render a new animation, we should start it from the beginning, I think. Though, I could see this not ending up being a good idea... NOTE: this is not just set to zero because it seems this causes a divide by zero error when fetching the keyframe.
		requestedAnimations.clear();
		
		return frame;
	}
}
