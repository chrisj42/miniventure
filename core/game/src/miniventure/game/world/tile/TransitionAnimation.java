package miniventure.game.world.tile;

import org.jetbrains.annotations.NotNull;

class TransitionAnimation {
	
	private final String name;
	private final boolean isEntrance;
	private final @NotNull TileType[] reqTiles;
	private final float frameRate;
	private float animationTime;
	
	TransitionAnimation(String spriteNameKey, boolean isEntrance, float frameDuration, @NotNull TileType... reqTiles) {
		this.isEntrance = isEntrance;
		this.frameRate = frameDuration;
		this.reqTiles = reqTiles;
		this.name = spriteNameKey;
	}
	
	String getName() { return name; }
	float getFrameRate() { return frameRate; }
	boolean isEntrance() { return isEntrance; }
	float getAnimationTime() { return animationTime; }
	
	@NotNull TileType[] getReqTiles() { return reqTiles; }
	
	void setAnimationTime(int numFrames) { animationTime = getFrameRate() * numFrames; }
}
