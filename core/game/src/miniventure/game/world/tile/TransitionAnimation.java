package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.EnumSet;

import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

public class TransitionAnimation extends RenderStyle {
	
	final String name;
	private final EnumSet<TileTypeEnum> triggerTypes;
	
	public TransitionAnimation(String name, float duration, TileTypeEnum... triggerTypes) { this(name, duration, PlayMode.NORMAL, triggerTypes); }
	public TransitionAnimation(String name, float duration, PlayMode playMode, TileTypeEnum... triggerTypes) {
		super(playMode, duration, false);
		
		this.name = name;
		this.triggerTypes = EnumSet.copyOf(Arrays.asList(triggerTypes));
		// if triggertypes is empty, then anything triggers it
	}
	
	public boolean isTriggerType(TileType type) { return isTriggerType(type.getEnumType()); }
	public boolean isTriggerType(TileTypeEnum type) {
		return triggerTypes.size() == 0 || triggerTypes.contains(type);
	}
	
	public float getDuration() { return time; }
}
