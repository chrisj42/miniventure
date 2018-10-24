package miniventure.game.world.tile;

import java.util.EnumSet;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TransitionAnimation extends RenderStyle {
	
	final String name;
	private final EnumSet<TileTypeEnum> triggerTypes;
	
	public TransitionAnimation(String name, float duration, TileTypeEnum... triggerTypes) { this(name, duration, PlayMode.NORMAL, triggerTypes); }
	public TransitionAnimation(String name, float duration, PlayMode playMode, TileTypeEnum... triggerTypes) {
		super(playMode, duration, false);
		
		this.name = name;
		this.triggerTypes = MyUtils.enumSet(triggerTypes);
		// if triggertypes is empty, then anything triggers it
	}
	
	public boolean isTriggerType(TileType type) { return isTriggerType(type.getEnumType()); }
	public boolean isTriggerType(TileTypeEnum type) {
		return triggerTypes.size() == 0 || triggerTypes.contains(type);
	}
	
	public float getDuration() { return time; }
	
	@Override
	TileAnimation<TextureHolder> getAnimation(@NotNull TileTypeEnum tileType, Array<TextureHolder> frames) {
		return new TileAnimation<TextureHolder>(sync, time/frames.size, frames, playMode) {
			@Override
			public TextureHolder getKeyFrame(Tile tile) {
				// this is for entrance animations on the client, as a way to stop them.
				TransitionManager man = tile.getType().getRenderer().transitionManager;
				if(man.playingAnimation(tile)) {
					if(!startedNonSync())
						man.resetAnimation(tile);
					float time = man.tryFinishAnimation(tile);
					if(!man.playingAnimation(tile))
						tile.updateSprites();
				}
				return super.getKeyFrame(tile);
			}
		};
	}
}
