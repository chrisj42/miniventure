package miniventure.game.world.entity.mob;

import java.util.EnumMap;
import java.util.concurrent.PriorityBlockingQueue;

import miniventure.game.network.GameProtocol.SpriteUpdate;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;
import miniventure.game.world.level.Level;

import com.badlogic.gdx.math.Rectangle;

import org.jetbrains.annotations.NotNull;

public class MobAnimationController<M extends Entity & Mob> {
	
	public enum AnimationState {
		/* this technically will be just for some more advanced functionality, but it is all built-in to the enum class.
			Enum implements Comparable, and compares based on the ordinal, I imagine.
			I will use the order of the enums to specify the "priority" of the animation state.
		*/
		/*BLOCK(2f), ATTACK(0.3f), RUN(0.3f), */WALK(.5f), IDLE(2f);
		
		public final float loopDuration;
		
		AnimationState(float loopDuration) { this.loopDuration = loopDuration; }
		
		public static final AnimationState[] values = values();
	}
	
	private M mob;
	private final String mobSpriteName;
	private final EnumMap<AnimationState, DirectionalAnimationRenderer> renderers;
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState = null, state = null;
	
	private boolean animationChanged = false;
	
	private PriorityBlockingQueue<AnimationState> requestedAnimations;
	private DirectionalAnimationRenderer renderer;
	
	public MobAnimationController(M mob, String spriteName) {
		this.mob = mob;
		mobSpriteName = spriteName;
		
		requestedAnimations = new PriorityBlockingQueue<>();
		
		renderers = new EnumMap<>(AnimationState.class);
		for(AnimationState state: AnimationState.values) {
			String textureName = mobSpriteName + '/' + state.name().toLowerCase() + '-';
			renderers.put(state, new DirectionalAnimationRenderer(mob.getDirection(), dir -> textureName + dir.name().toLowerCase(), state.loopDuration, false, true));
		}
		
		progressAnimation(0);
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	public synchronized void requestState(@NotNull AnimationState rState) {
		try {
			requestedAnimations.add(rState);
		} catch(NullPointerException ex) {
			System.err.println("Error when requesting new mob animation state. pri queue: "+requestedAnimations);
			ex.printStackTrace();
		}
	}
	
	public synchronized SpriteUpdate getSpriteUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return new SpriteUpdate(renderer);
	}
	
	public synchronized boolean setDirection(@NotNull Direction dir) {
		if(mob.getDirection() == dir)
			return false; // no change.
		
		// check if the mob can fit facing this new direction.
		Rectangle oldRect = mob.getBounds();
		renderer.setDirection(dir);
		Rectangle newRect = mob.getBounds();
		
		// if the new bounds isn't new space, then there's no need to check for collisions.
		if(!oldRect.equals(newRect) && !oldRect.contains(newRect)) {
			Level level = mob.getLevel();
			// if level is null, we cannot check if the movement is valid.
			boolean valid = level == null || Entity.checkMove(mob, level, oldRect, newRect);
			if(!valid) {
				renderer.setDirection(mob.getDirection());
				return false; // different directional sprite causes new collisions.
			}
		}
		
		renderer.setDirection(dir);
		return true;
	}
	
	public synchronized void progressAnimation(float delta) {
		// update the animation
		if(renderer != null)
			renderer.update(delta);
		
		// reset the animation
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		
		if(state != prevState) {
			animationChanged = true;
			
			renderer = renderers.get(state);
			renderer.resetAnimation();
			renderer.setDirection(mob.getDirection());
			mob.setRenderer(renderer);
		}
		requestedAnimations.clear();
	}
}
