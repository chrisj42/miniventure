package miniventure.game.world.entity.mob;

import java.util.PriorityQueue;

import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.AnimationRenderer;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;

import org.jetbrains.annotations.NotNull;

public class MobAnimationController<M extends Entity & Mob> {
	
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
		BLOCK(1.0f), ATTACK(0.15f), RUN(0.1f), WALK(0.5f), IDLE(1.0f);
		
		public final float frameDuration;
		
		AnimationState(float frameDuration) { this.frameDuration = frameDuration; }
		
		public static final AnimationState[] values = values();
	}
	
	private M mob;
	private final String mobSpriteName;
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState = null, state = null;
	
	//private float animationTime;
	//private String previousAnimation = "";
	
	private boolean animationChanged = false;
	
	private PriorityQueue<AnimationState> requestedAnimations;
	private DirectionalAnimationRenderer renderer;
	
	//private TextureRegion curTexture;
	
	public MobAnimationController(M mob, String spriteName) {
		//state = AnimationState.IDLE;
		this.mob = mob;
		mobSpriteName = spriteName;
		
		requestedAnimations = new PriorityQueue<>();
		
		progressAnimation(0);
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	void requestState(AnimationState rState) { requestedAnimations.add(rState); }
	
	public SpriteUpdate getSpriteUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return new SpriteUpdate(renderer);
	}
	
	/*public DirectionalAnimationRenderer getRendererUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return renderer;
	}*/
	
	void setDirection(@NotNull Direction dir) { renderer.setDirection(dir); }
	
	void progressAnimation(float delta) {
		// update the animation
		// animationTime += delta;
		if(renderer != null)
			renderer.update(delta);
		//if(animationTime > ani.getAnimationDuration()) animationTime = 0; // reset the animationTime to prevent any possibility of overflow
		//TextureRegion frame = ani.getKeyFrame(animationTime, true);
		//renderer.setDirection(mob.getDirection());
		
		// reset the animation
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		if(state != prevState) {
			animationChanged = true; // if we're going to render a new animation, we should start it from the beginning, I think. Though, I could see this not ending up being a good idea... NOTE: this is not just set to zero because it seems this causes a divide by zero error when fetching the keyframe.
			
			String textureName = mobSpriteName + "/" + state.name().toLowerCase() + "-";
			renderer = new DirectionalAnimationRenderer(mob.getDirection(), dir -> textureName+dir.name().toLowerCase(), state.frameDuration);
			mob.setRenderer(renderer);
		}
		requestedAnimations.clear();
	}
}
