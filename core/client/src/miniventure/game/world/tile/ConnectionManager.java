package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ConnectionManager {
	
	static EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	
	public static ConnectionManager DEFAULT(@NotNull TileTypeEnum type) {
		return NO_CONNECT(type, RenderStyle.SINGLE_FRAME);
	}
	public static ConnectionManager NO_CONNECT(@NotNull TileTypeEnum type, RenderStyle style) {
		return new ConnectionManager(type, style);
	}
	
	private final TileTypeEnum type;
	private final RenderStyle renderStyle;
	private final EnumSet<TileTypeEnum> connectingTypes;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	public ConnectionManager(@NotNull TileTypeEnum type, RenderStyle renderStyle, TileTypeEnum... connectingTypes) {
		this.type = type;
		this.renderStyle = renderStyle;
		this.connectingTypes = MyUtils.enumSet(connectingTypes);
	}
	
	public ConnectionManager overrideSprite(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	
	/// Checks the given aroundTypes for all types 
	@NotNull
	public TileAnimation<TextureHolder> getConnectionSprite(EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		if(connectingTypes.size() == 0)
			return renderStyle.getAnimation(type, "00", tileAnimations);
		
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
		
		return overrides.getOrDefault(spriteIdx, renderStyle).getAnimation(type, (spriteIdx<10?"0":"")+spriteIdx, tileAnimations);
	}
	
}
