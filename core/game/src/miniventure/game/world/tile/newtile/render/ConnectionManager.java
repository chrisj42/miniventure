package miniventure.game.world.tile.newtile.render;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.world.tile.TileTouchCheck;
import miniventure.game.world.tile.newtile.TileLayer;
import miniventure.game.world.tile.newtile.TileType;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

public class ConnectionManager {
	
	static EnumMap<TileType, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileType.class);
	
	private final RenderStyle renderStyle;
	private final EnumSet<TileType> connectingTypes;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public ConnectionManager(RenderStyle renderStyle, TileType... connectingTypes) {
		this.renderStyle = renderStyle;
		this.connectingTypes = EnumSet.copyOf(Arrays.asList(connectingTypes));
	}
	
	public ConnectionManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	public Animation<TextureHolder> getConnectionSprite(TileLayer[][] aroundTypes) {
		TileLayer type = aroundTypes[1][1];
		
		if(connectingTypes.size() == 0)
			return renderStyle.getAnimation(tileAnimations.get(type.getType()).get("00"));
		
		boolean[] tileConnections = new boolean[9];
		
		for(int i = 0; i < aroundTypes.length; i++) {
			// find the top opaque one
			boolean connects = false;
			for(int ti = aroundTypes[i].length - 1; ti >= 0; ti--) {
				if(connectingTypes.contains(aroundTypes[i][ti].getType())) {
					connects = true;
					break;
				}
				if(aroundTypes[i][ti].getRenderer().isOpaque()) // the type also doesn't connect, at this point.
					break; // lower tiles are irrelevant.
			}
			
			tileConnections[i] = connects;
		}
		
		int spriteIdx = 0;
		for (int i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
			if (TileTouchCheck.connectionChecks[i].checkMatch(tileConnections)) {
				spriteIdx = i;
				break;
			}
		}
		
		return overrides.getOrDefault(spriteIdx, renderStyle).getAnimation(tileAnimations.get(type.getType()).get((spriteIdx<10?"0":"")+spriteIdx));
	}
	
}
