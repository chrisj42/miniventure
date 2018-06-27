package miniventure.game.world.entity.mob;

import java.util.concurrent.PriorityBlockingQueue;

import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;

import org.jetbrains.annotations.NotNull;

public class MobAnimationController<M extends Entity & Mob> {
	
	public enum AnimationState {
		/* this technically will be just for some more advanced functionality, but it is all built-in to the enum class.
			Enum implements Comparable, and compares based on the ordinal, I imagine.
			I will use the order of the enums to specify the "priority" of the animation state.
		*/
		BLOCK(2f), ATTACK(0.3f), RUN(0.3f), WALK(.5f), IDLE(2f);
		
		public final float loopDuration;
		
		AnimationState(float loopDuration) { this.loopDuration = loopDuration; }
		
		public static final AnimationState[] values = values();
	}
	
	private M mob;
	private final String mobSpriteName;
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState = null, state = null;
	
	private boolean animationChanged = false;
	
	private PriorityBlockingQueue<AnimationState> requestedAnimations;
	private DirectionalAnimationRenderer renderer;
	
	public MobAnimationController(M mob, String spriteName) {
		this.mob = mob;
		mobSpriteName = spriteName;
		
		requestedAnimations = new PriorityBlockingQueue<>();
		
		progressAnimation(0);
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	void requestState(@NotNull AnimationState rState) {
		try {
			requestedAnimations.add(rState);
		} catch(NullPointerException ex) {
			System.err.println("Error when requesting new mob animation state. pri queue: "+requestedAnimations);
			ex.printStackTrace();
		}
	}
	
	public SpriteUpdate getSpriteUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return new SpriteUpdate(renderer);
	}
	
	void setDirection(@NotNull Direction dir) { renderer.setDirection(dir); }
	
	void progressAnimation(float delta) {
		// update the animation
		if(renderer != null)
			renderer.update(delta);
		
		// reset the animation
		prevState = state;
		try {
			state = requestedAnimations.poll();
			if(state == null) state = AnimationState.IDLE;
		} catch(NullPointerException ex) {
			state = AnimationState.IDLE;
		}
		if(state != prevState) {
			animationChanged = true;
			
			String textureName = mobSpriteName + "/" + state.name().toLowerCase() + "-";
			renderer = new DirectionalAnimationRenderer(mob.getDirection(), dir -> textureName+dir.name().toLowerCase(), state.loopDuration, false, true);
			mob.setRenderer(renderer);
		}
		requestedAnimations.clear();
	}
}
