package miniventure.game.world.entity.particle;

import miniventure.game.util.blinker.FrameBlinker;
import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class LifetimeTracker {
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	
	private final Entity entity;
	private final float lifetime;
	
	private float time; // the current time relative to the creation of this item entity. used as the current position along the "x-axis".
	
	public LifetimeTracker(@NotNull Entity e, float lifetime) { this(e, lifetime, true); }
	public LifetimeTracker(@NotNull Entity e, float lifetime, boolean blinker) {
		this.entity = e;
		this.lifetime = lifetime;
		
		if(blinker)
			e.setBlinker(null, lifetime * BLINK_THRESHOLD, false, new FrameBlinker(1, 1, false));
	}
	
	LifetimeTracker(@NotNull Entity e, float lifetime, float elapsedTime) {
		this.entity = e;
		this.lifetime = lifetime;
		this.time = elapsedTime;
	}
	
	float getLifetime() { return lifetime; }
	float getTime() { return time; }
	
	public void update(float delta) {
		time += delta;
		if(time > lifetime)
			entity.remove();
	}
}
