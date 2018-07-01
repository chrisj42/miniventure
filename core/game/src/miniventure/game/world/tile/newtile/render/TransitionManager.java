package miniventure.game.world.tile.newtile.render;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.newtile.TileType;

import com.badlogic.gdx.utils.Array;

public class TransitionManager {
	
	static EnumMap<TileType, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileType.class);
	
	private final HashMap<String, TransitionAnimation> entranceAnimations = new HashMap<>();
	private final HashMap<String, TransitionAnimation> exitAnimations = new HashMap<>();
	
	// these are Tile-specific
	// private TransitionAnimation curAnimation;
	// private boolean entering, exiting;
	
	// store this in DataMap, along with trans start time?
	public enum TransitionMode {
		ENTERING, EXITING, NONE
	}
	
	public TransitionManager() {
		
	}
	
	public TransitionManager addEntranceAnimations(TransitionAnimation... animations) {
		for(TransitionAnimation transition: animations)
			entranceAnimations.put(transition.name, transition);
		
		return this;
	}
	
	public TransitionManager addExitAnimations(TransitionAnimation... animations) {
		for(TransitionAnimation transition: animations)
			exitAnimations.put(transition.name, transition);
		
		return this;
	}
	
}
