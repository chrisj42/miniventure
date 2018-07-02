package miniventure.game.world.tile.newtile;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

public class TransitionAnimation extends RenderStyle {
	
	final String name;
	private final TileType[] triggerTypes;
	
	public TransitionAnimation(String name, float duration, TileType... triggerTypes) { this(name, duration, PlayMode.NORMAL, triggerTypes); }
	public TransitionAnimation(String name, float duration, PlayMode playMode, TileType... triggerTypes) {
		super(playMode, duration, false);
		
		this.name = name;
		this.triggerTypes = triggerTypes;
		// if triggertypes is empty, then anything triggers it
	}
	
}
