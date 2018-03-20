package miniventure.game.world.tile;

import miniventure.game.world.tilenew.Tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ConnectionProperty implements TileProperty {
	
	private final boolean connects;
	private final Array<TileType> connectingTiles;
	
	private TileType tileType;
	
	ConnectionProperty(boolean connects, TileType... connectingTiles) {
		this.connects = connects;
		this.connectingTiles = new Array<>(connectingTiles);
	}
	
	@Override
	public void init(@NotNull TileType type) {
		addConnectingType(type);
		this.tileType = type;
	}
	
	void addConnectingType(TileType type) {
		if(!connectingTiles.contains(type, true))
			connectingTiles.add(type);
	}
	
	AtlasRegion getSprite(Tile tile, TileType[][] aroundTypes) {
		int spriteIdx = 0;
		
		if(connects) {
			boolean[] tileConnections = new boolean[9];
			tileConnections[4] = true;
			
			for(int i = 0; i < aroundTypes.length; i++) {
				// Note that THE TILE MATCHING THIS ONE CONTAINS NO TYPES. So you must manually skip the center, and set it to true.
				if(i == 4) continue;
				
				// find the top opaque one
				boolean connects = false;
				for(int ti = aroundTypes[i].length - 1; ti >= 0; ti--) {
					if(connectingTiles.contains(aroundTypes[i][ti], true)) {
						connects = true;
						break;
					}
					if(aroundTypes[i][ti].getProp(AnimationProperty.class).isOpaque()) // the type also doesn't connect, at this point.
						break; // lower tiles are irrelevant.
				}
				
				tileConnections[i] = connects;
			}
			
			for (int i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
				if (TileTouchCheck.connectionChecks[i].checkMatch(tileConnections)) {
					spriteIdx = i;
					break;
				}
			}
		}
		
		return tileType.getProp(AnimationProperty.class).getSprite(spriteIdx, false, tile);
	}
	
	@Override
	public Class<? extends TileProperty> getUniquePropertyClass() { return ConnectionProperty.class; }
}
