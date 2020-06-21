package miniventure.game.world.entity.mob;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.DirectionAnimationMap;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.management.Level;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;

import org.jetbrains.annotations.NotNull;

public class MobAnimationController {
	
	public enum AnimationState {
		/* this technically will be just for some more advanced functionality, but it is all built-in to the enum class.
			Enum implements Comparable, and compares based on the ordinal, I imagine.
			I will use the order of the enums to specify the "priority" of the animation state.
		*/
		/*BLOCK(2f), ATTACK(0.3f), RUN(0.3f), */WALK(.25f), IDLE(2f);
		
		public final float frameRate;
		
		AnimationState(float frameRate) { this.frameRate = frameRate; }
		
		public static final AnimationState[] values = values();
	}
	
	private static final HashMap<String, EnumMap<AnimationState, DirectionAnimationMap>> mobAnimationSets = new HashMap<>();
	
	private Mob mob;
	private final String mobSpriteName;
	private final EnumMap<AnimationState, DirectionAnimationMap> animationMaps;
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState = null, state = null;
	
	// private boolean animationChanged = false;
	
	private PriorityBlockingQueue<AnimationState> requestedAnimations;
	// private DirectionalAnimationRenderer renderer;
	private Direction dir;
	private float startTime = -1;
	private Animation<TextureHolder> animation;
	
	public MobAnimationController(Mob mob, String spriteName) {
		this.mob = mob;
		mobSpriteName = spriteName;
		
		requestedAnimations = new PriorityBlockingQueue<>();
		
		animationMaps = mobAnimationSets.computeIfAbsent(spriteName, name -> {
			EnumMap<AnimationState, DirectionAnimationMap> animMaps = new EnumMap<>(AnimationState.class);
			for(AnimationState state: AnimationState.values) {
				String textureName = name + '/' + state.name().toLowerCase() + '-';
				animMaps.put(state, new DirectionAnimationMap(dir -> textureName + dir.name().toLowerCase(), state.frameRate, true, true));
			}
			return animMaps;
		});
		
		reset();
	}
	
	void reset() {
		dir = mob.getDirection();
		prevState = null;
		state = null;
		startTime = -1;
		animation = animationMaps.get(AnimationState.IDLE).getAnimation(dir);
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	public void requestState(@NotNull AnimationState rState) {
		try {
			requestedAnimations.add(rState);
		} catch(NullPointerException ex) {
			System.err.println("Error when requesting new mob animation state. pri queue: "+requestedAnimations);
			ex.printStackTrace();
		}
	}
	
	/*public synchronized SpriteUpdate getSpriteUpdate() {
		if(!animationChanged) return null;
		animationChanged = false;
		return new SpriteUpdate(renderer);
	}*/
	
	public TextureHolder getSprite(float gameTime) {
		if(startTime < 0)
			startTime = gameTime;
		
		return animation.getKeyFrame(gameTime - startTime);
	}
	
	public boolean setDirection(@NotNull Direction dir) {
		if(mob.getDirection() == dir)
			return false; // no change.
		
		// check if the mob can fit facing this new direction.
		Rectangle oldRect = mob.getBounds();
		// renderer.setDirection(dir);
		this.dir = dir;
		Rectangle newRect = mob.getBounds();
		
		// if the new bounds isn't new space, then there's no need to check for collisions.
		if(!oldRect.equals(newRect) && !oldRect.contains(newRect)) {
			Level level = mob.getLevel();
			// if level is null, we cannot check if the movement is valid.
			boolean valid = level == null || Entity.checkMove(mob, level, oldRect, newRect);
			if(!valid) {
				// renderer.setDirection(mob.getDirection());
				this.dir = mob.getDirection();
				return false; // different directional sprite causes new collisions.
			}
		}
		
		// renderer.setDirection(dir);
		return true;
	}
	
	public void progressAnimation() {
		// update the animation
		// if(renderer != null)
		// 	renderer.update(delta);
		
		// reset the animation
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		
		if(state != prevState) {
			// animationChanged = true;
			
			// renderer = animationMaps.get(state);
			// renderer.resetAnimation();
			// renderer.setDirection(mob.getDirection());
			// mob.setRenderer(renderer);
			startTime = -1;
			animation = animationMaps.get(state).getAnimation(dir);
		}
		requestedAnimations.clear();
	}
}
