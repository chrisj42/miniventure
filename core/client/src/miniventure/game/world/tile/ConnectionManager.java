package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ConnectionManager {
	
	static EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> tileAnimations = new EnumMap<>(TileTypeEnum.class);
	static EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> mainAnimations = new EnumMap<>(TileTypeEnum.class);
	private static final HashMap<String, Array<TextureHolder>> dummy = new HashMap<>(0);
	
	private final TileTypeEnum type;
	private final RenderStyle renderStyle;
	private final EnumSet<TileTypeEnum> connectingTypes;
	private final ConnectionCheck connectionCheck;
	private final HashMap<Integer, RenderStyle> overrides = new HashMap<>();
	
	@FunctionalInterface
	interface ConnectionCheck {
		boolean connects(RenderTile tile, TileTypeEnum adjacentType);
		
		static ConnectionCheck list(TileTypeEnum... connectingTypes) {
			final EnumSet<TileTypeEnum> matches = MyUtils.enumSet(connectingTypes);
			return (tile, type) -> matches.contains(type);
		}
	}
	
	// because the default sprite is part of the connecting sprite system, a "no connections" method/constructor is illogical; instead, the extra sprites are detected, and if found, they are "enabled".
	public ConnectionManager(@NotNull TileTypeEnum type, TileTypeEnum... connectingTypes) {
		this(type, RenderStyle.SINGLE_FRAME, connectingTypes);
	}
	public ConnectionManager(@NotNull TileTypeEnum type, RenderStyle renderStyle, TileTypeEnum... connectingTypes) {
		this.type = type;
		this.renderStyle = renderStyle;
		
		if(!tileAnimations.containsKey(type)) // debug check
			System.out.println("warning: tiletype "+type+" has no connection sprites.");
		
		// if there is only one animation for this tiletype, then the connecting types are irrelevant because there's only one sprite to choose from.
		if(tileAnimations.getOrDefault(type, dummy).size() <= 1) {
			if(connectingTypes.length > 1) // so this doesn't kick me in the butt later, aka make me wonder what the issue is for hours
				System.out.println("warning: multiple connecting types specified for "+type+", but there are not multiple connect animations; ignoring connections.");
			this.connectingTypes = EnumSet.noneOf(TileTypeEnum.class);
		}
		else {
			if(connectingTypes.length == 0) {// there are multiple connection sprites, but no connecting types; add the self as a connecting type, so the extra sprites are used.
				connectingTypes = new TileTypeEnum[] {type};
				// GameCore.debug("connect sprites found for "+type+" with no types, adding self");
			}
			
			this.connectingTypes = MyUtils.enumSet(connectingTypes);
		}
		
		connectionCheck = ConnectionCheck.list(connectingTypes);
	}
	public ConnectionManager(@NotNull TileTypeEnum type, ConnectionCheck connectionCheck) {
		this(type, RenderStyle.SINGLE_FRAME, connectionCheck);
	}
	public ConnectionManager(@NotNull TileTypeEnum type, RenderStyle renderStyle, ConnectionCheck connectionCheck) {
		this.type = type;
		this.renderStyle = renderStyle;
		this.connectionCheck = connectionCheck;
		
		if(!tileAnimations.containsKey(type)) // debug check
			System.out.println("warning: tiletype "+type+" has no connection sprites.");
		
		connectingTypes = null;
	}
	
	public ConnectionManager customStyle(int spriteIndex, RenderStyle newStyle) {
		overrides.put(spriteIndex, newStyle);
		return this;
	}
	
	private void addAnimations(LinkedList<TileAnimation> sprites, RenderTile tile, int spriteIdx) {
		if(mainAnimations.containsKey(type)) {
			int idx = new Random(tile.x * 17 + tile.y * 131).nextInt(mainAnimations.get(type).size());
			sprites.add(RenderStyle.SINGLE_FRAME.getAnimation(type, (idx<10?"0":"")+idx, mainAnimations, "connection main"));
		}
		sprites.add(overrides.getOrDefault(spriteIdx, renderStyle).getAnimation(type, (spriteIdx<10?"0":"")+spriteIdx, tileAnimations, "connection"));
	}
	
	/// Checks the given aroundTypes for all types
	public void addConnectionSprites(LinkedList<TileAnimation> sprites, RenderTile tile, EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		if(connectingTypes != null && connectingTypes.size() == 0) {
			addAnimations(sprites, tile, 0);
			return;
		}
		
		EnumMap<RelPos, Boolean> tileConnections = new EnumMap<>(RelPos.class);
		
		for(RelPos rp: RelPos.values) {
			// check if each surrounding tile has something in the connectingTypes array
			boolean connects = false;
			for(TileTypeEnum aroundType: aroundTypes.get(rp)) {
				if(connectionCheck.connects(tile, aroundType)) {
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
		
		addAnimations(sprites, tile, spriteIdx);
	}
	
}
