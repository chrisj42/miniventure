package miniventure.game.world.tile;

import java.util.EnumSet;

import miniventure.game.util.MyUtils;

class TileTransition {
	
	final TileAnimation animation;
	// private final boolean entering;
	// private final float duration;
	private final EnumSet<TileType> triggerTypes;
	
	TileTransition(TileAnimation animation, TileType... triggerTypes) {
		this.animation = animation;
		// this.name = name;
		// this.duration = animation.getAnimationDuration();
		this.triggerTypes = MyUtils.enumSet(triggerTypes);
		// if triggertypes is empty, then anything triggers it
	}
	
	// boolean isTriggerType(TileType type) { return isTriggerType(type); }
	boolean isTriggerType(TileType type) {
		return triggerTypes.size() == 0 || triggerTypes.contains(type);
	}
	
	float getDuration() { return animation.getAnimationDuration(); }
}
