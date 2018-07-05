package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ConnectionManager {
	
	static EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	private final TileTypeEnum type;
	private final RenderStyle renderStyle;
	private final EnumSet<TileTypeEnum> connectingTypes;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public ConnectionManager(@NotNull TileTypeEnum type, RenderStyle renderStyle, TileTypeEnum... connectingTypes) {
		this.type = type;
		this.renderStyle = renderStyle;
		this.connectingTypes = connectingTypes.length == 0 ? EnumSet.noneOf(TileTypeEnum.class) : EnumSet.copyOf(Arrays.asList(connectingTypes));
	}
	
	public ConnectionManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	
	/// Checks the given aroundTypes for all types 
	@NotNull
	public TileAnimation<TextureHolder> getConnectionSprite(EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		if(connectingTypes.size() == 0)
			return renderStyle.getAnimation(tileAnimations.get(type).get("00"));
		
		EnumMap<RelPos, Boolean> tileConnections = new EnumMap<>(RelPos.class);
		
		for(RelPos rp: RelPos.values) {
			// check if each surrounding tile has something in the connectingTypes array
			boolean connects = false;
			for(TileTypeEnum aroundType: aroundTypes.get(rp)) {
				if(connectingTypes.contains(aroundType)) {
					connects = true;
					break;
				}
			}
			tileConnections.put(rp, connects);
		}
		
		int spriteIdx = 0;
		for (int i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
			if (TileTouchCheck.connectionChecks[i].checkMatch(tileConnections)) {
				spriteIdx = i;
				break;
			}
		}
		
		return overrides.getOrDefault(spriteIdx, renderStyle).getAnimation(tileAnimations.get(type).get((spriteIdx<10?"0":"")+spriteIdx));
	}
	
}
