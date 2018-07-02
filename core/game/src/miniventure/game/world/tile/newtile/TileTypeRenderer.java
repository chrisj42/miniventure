package miniventure.game.world.tile.newtile;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.RelPos;
import miniventure.game.world.tile.newtile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class TileTypeRenderer {
	
	static {
		Array<TextureHolder> regions = GameCore.tileAtlas.getRegions();
		for(TextureHolder region: regions) {
			TileTypeEnum tileType = TileTypeEnum.valueOf(region.name.substring(0, region.name.indexOf("/")).toUpperCase());
			String spriteID = region.name.substring(region.name.indexOf("/")+1);
			
			String prefix = spriteID.substring(0, 1);
			spriteID = spriteID.substring(1).toLowerCase();
			
			EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> animationMap;
			if(prefix.equals("c"))
				animationMap = ConnectionManager.tileAnimations;
			else if(prefix.equals("o"))
				animationMap = OverlapManager.tileAnimations;
			else if(prefix.equals("t"))
				animationMap = TransitionManager.tileAnimations;
			else {
				System.err.println("Unknown Tile Sprite Frame for "+tileType+": "+prefix+spriteID);
				continue;
			}
			
			animationMap.computeIfAbsent(tileType, k -> new HashMap<>());
			animationMap.get(tileType).computeIfAbsent(spriteID, k -> new Array<>());
			animationMap.get(tileType).get(spriteID).add(region);
		}
	}
	
	
	private final TileTypeEnum tileType;
	private final boolean isOpaque;
	private final ConnectionManager connectionManager;
	private final OverlapManager overlapManager;
	private final TransitionManager transitionManager;
	
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager) {
		this(tileType, isOpaque, connectionManager, OverlapManager.NONE(tileType));
	}
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager) {
		this(tileType, isOpaque, connectionManager, overlapManager, new TransitionManager());
	}
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager, TransitionManager transitionManager) {
		this.tileType = tileType;
		this.isOpaque = isOpaque;
		this.connectionManager = connectionManager;
		this.overlapManager = overlapManager;
		this.transitionManager = transitionManager;
	}
	
	public boolean isOpaque() { return isOpaque; }
	
	
	// whenever a tile changes its TileTypeEnum stack in any way, all 9 tiles around it re-fetch their overlap and main animations. Then they keep that stack of animations until the next fetch.
	
	// gets the sprite for when this tiletype is surrounded by the given types.
	public Animation<TextureHolder> getConnectionSprite(EnumSet<TileTypeEnum>[] aroundTypes) {
		// TODO check for current transition
		return connectionManager.getConnectionSprite(aroundTypes);
	}
	
	// gets the overlap sprite (sides + any isolated corners) for this tiletype overlapping a tile at the given positions.
	public Array<Animation<TextureHolder>> getOverlapSprites(EnumSet<RelPos> overlapPositions) {
		return overlapManager.getOverlapSprites(overlapPositions);
	}
	
}
