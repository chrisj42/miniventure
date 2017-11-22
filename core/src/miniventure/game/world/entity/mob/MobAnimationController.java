package miniventure.game.world.entity.mob;

import java.util.HashMap;
import java.util.PriorityQueue;

import miniventure.game.world.entity.Direction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MobAnimationController {
	
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
	private TextureAtlas atlas; // TODO this needs to be redone so that there is a single, static animator instance of each type of mob, so that they are not continuously created, which is not efficient.
	/** @noinspection FieldCanBeLocal*/
	private AnimationState prevState, state;
	
	private float animationTime;
	private HashMap<String, Animation<TextureRegion>> animations;
	
	private PriorityQueue<AnimationState> requestedAnimations;
	
	public MobAnimationController(Mob mob) {
		state = AnimationState.IDLE;
		this.mob = mob;
		
		requestedAnimations = new PriorityQueue<>();
		
		animations = new HashMap<>();
		
		atlas = new TextureAtlas("sprites/"+mob.getSpriteName()+".txt");
		for(AnimationState state: AnimationState.values) {
			for(String dir: Direction.names) {
				String name = state.name().toLowerCase()+"-"+dir;
				animations.put(name, new Animation<>(state.frameDuration, atlas.findRegions(name)));
				//atlas.dispose(); // can NOT do this! disposing of the atlas disposes of all the textures too.
			}
		}
	}
	
	public void dispose() {
		atlas.dispose();
	}
	
	// the animation time is reset in getFrame, so this will never overflow.
	public void update() { update(Gdx.graphics.getDeltaTime()); }
	public void update(float delta) { animationTime += delta; }
	
	void requestState(AnimationState rState) { requestedAnimations.add(rState); }
	
	// this is going to be called once per render, and once per functionality tick, so it makes sense to clear the animations whenever it is called.
	public TextureRegion getFrame() {
		prevState = state;
		state = requestedAnimations.poll();
		if(state == null) state = AnimationState.IDLE;
		if(state != prevState)
			animationTime = Gdx.graphics.getDeltaTime(); // if we're going to render a new animation, we should start it from the beginning, I think. Though, I could see this not ending up being a good idea... NOTE: this is not just set to zero because it seems this causes a divide by zero error when fetching the keyframe.
		requestedAnimations.clear();
		
		// Combines the direction with the animation state
		String textureName = state.name().toLowerCase() + "-" + mob.getDirection().name().toLowerCase();
		Animation<TextureRegion> ani = animations.get(textureName);
		float duration = ani.getAnimationDuration();
		if(animationTime > duration) animationTime = 0;
		return ani.getKeyFrame(animationTime, true);
	}
}
