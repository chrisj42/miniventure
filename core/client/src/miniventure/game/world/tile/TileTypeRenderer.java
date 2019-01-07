package miniventure.game.world.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.InstanceCounter;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileTypeRenderer {
	
	static {
		Array<TextureHolder> regions = GameCore.tileAtlas.getRegions();
		for(TextureHolder region: regions) {
			TileTypeEnum tileType = TileTypeEnum.valueOf(region.name.substring(0, region.name.indexOf('/')).toUpperCase());
			String spriteID = region.name.substring(region.name.indexOf('/')+1);
			
			String prefix = spriteID.substring(0, 1);
			spriteID = spriteID.substring(1).toLowerCase();
			
			EnumMap<TileTypeEnum, HashMap<String, Array<TextureHolder>>> animationMap;
			switch(prefix) {
				case "c": animationMap = ConnectionManager.tileAnimations; break;
				case "o": animationMap = OverlapManager.tileAnimations; break;
				case "t": animationMap = TransitionAnimation.tileAnimations; break;
				default:
					if(!(prefix + spriteID).equals("swim"))
						System.err.println("Unknown Tile Sprite Frame for " + tileType + ": " + prefix + spriteID);
					continue;
			}
			
			animationMap
				.computeIfAbsent(tileType, k -> new HashMap<>())
				.computeIfAbsent(spriteID, k -> new Array<>(TextureHolder.class))
				.add(region);
		}
	}
	
	
	private final TileTypeEnum tileType;
	private final boolean isOpaque;
	private final ConnectionManager connectionManager;
	private final OverlapManager overlapManager;
	
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque) {
		this(tileType, isOpaque, new ConnectionManager(tileType, RenderStyle.SINGLE_FRAME));
	}
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager) {
		this(tileType, isOpaque, connectionManager, OverlapManager.NONE(tileType));
	}
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, OverlapManager overlapManager) {
		this(tileType, isOpaque, new ConnectionManager(tileType, RenderStyle.SINGLE_FRAME), overlapManager);
	}
	public TileTypeRenderer(@NotNull TileTypeEnum tileType, boolean isOpaque, ConnectionManager connectionManager, OverlapManager overlapManager) {
		this.tileType = tileType;
		this.isOpaque = isOpaque;
		this.connectionManager = connectionManager;
		this.overlapManager = overlapManager;
	}
	
	public TileTypeRenderer(@NotNull TileTypeRenderer model, @Nullable ConnectionManager connectionManager, @Nullable OverlapManager overlapManager) {
		this.tileType = model.tileType;
		isOpaque = model.isOpaque;
		this.connectionManager = connectionManager == null ? model.connectionManager : connectionManager;
		this.overlapManager = overlapManager == null ? model.overlapManager : overlapManager;
	}
	
	public boolean isOpaque() { return isOpaque; }
	
	// whenever a tile changes its TileTypeEnum stack in any way, all 9 tiles around it re-fetch their overlap and main animations. Then they keep that stack of animations until the next fetch.
	
	// gets the sprite for when this tiletype is surrounded by the given types.
	public TileAnimation<TextureHolder> getConnectionSprite(@NotNull Tile tile, EnumMap<RelPos, EnumSet<TileTypeEnum>> aroundTypes) {
		String name = tile.getDataMap(tileType).get(TileCacheTag.TransitionName);
		if(name != null)
			return ClientTileType.get(tileType).getTransition(name).getAnimation();
		
		return connectionManager.getConnectionSprite(aroundTypes);
	}
	
	// gets the overlap sprite (sides + any isolated corners) for this tiletype overlapping a tile at the given positions.
	public ArrayList<TileAnimation<TextureHolder>> getOverlapSprites(InstanceCounter<RelPos> overlapPositions) {
		return overlapManager.getOverlapSprites(overlapPositions);
	}
	
}
