package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.Direction;

public enum ActionType {
	SLASH(0.35f, true),
	PUNCH(0.3f, true),
	IMPACT(0.4f, false);
	
	public final float animationTime;
	private final boolean directional;
	
	ActionType(float animationTime, boolean directional) {
		this.animationTime = animationTime;
		this.directional = directional;
	}
	
	public String getSpriteName(Direction dir) {
		return "particle/"+name().toLowerCase()+(directional?'-'+dir.name().toLowerCase():"");
	}
}
