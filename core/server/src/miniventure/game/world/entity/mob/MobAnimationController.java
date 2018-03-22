package miniventure.game.world.entity.mob;

import java.util.PriorityQueue;

import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;

public class MobAnimationController {
	
	/*private static final HashMap<String, HashMap<String, Animation<TextureRegion>>> mobAnimations = new HashMap<>();
	
	private static HashMap<String, Animation<TextureRegion>> getMobAnimation(String mobSpriteName) {
		HashMap<String, Animation<TextureRegion>> animations = mobAnimations.get(mobSpriteName);
		
		if(animations == null) {
			animations = new HashMap<>();
			
			for (AnimationState state : AnimationState.values) {
				for (String dir : Direction.names) {
					String name = state.name().toLowerCase() + "-" + dir;
					animations.put(name, new Animation<>(state.frameDuration, GameCore.entityAtlas.findRegions(mobSpriteName + "/" + name)));
				}
			}
			
			mobAnimations.put(mobSpriteName, animations);
		}
		
		return animations;
	}*/
	
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
	private final String mobSpriteName;
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState, state;
	
	private float animationTime;
	private String previousAnimation = "";
	
	private boolean animationChanged = false;
	
	private PriorityQueue<AnimationState> requestedAnimations;
	
	//private TextureRegion curTexture;
	
	public MobAnimationController(Mob mob, String spriteName) {
		state = AnimationState.IDLE;
		this.mob = mob;
		mobSpriteName = spriteName;
		
		requestedAnimations = new PriorityQueue<>();
		
		//animations = getMobAnimation(spriteName);
		
		progressAnimation(0);
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	void requestState(AnimationState rState) { requestedAnimations.add(rState); }
	
	public SpriteUpdate getSpriteUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return new SpriteUpdate(new AnimationRenderer(previousAnimation, prevState.frameDuration, true));
	}
	
	void progressAnimation(float delta) {
		// update the animation
		animationTime += delta;
		// fetch the current frame
		String textureName = mobSpriteName + "/" + state.name().toLowerCase() + "-" + mob.getDirection().name().toLowerCase();
		
		if(!textureName.equals(previousAnimation))
			animationChanged = true;
		
		//if(animationTime > ani.getAnimationDuration()) animationTime = 0; // reset the animationTime to prevent any possibility of overflow
		//TextureRegion frame = ani.getKeyFrame(animationTime, true);
		
		
		// reset the animation
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		if(state != prevState) {
			animationTime = 0; // if we're going to render a new animation, we should start it from the beginning, I think. Though, I could see this not ending up being a good idea... NOTE: this is not just set to zero because it seems this causes a divide by zero error when fetching the keyframe.
		}
		requestedAnimations.clear();
		
		
		this.previousAnimation = textureName;
	}
}
