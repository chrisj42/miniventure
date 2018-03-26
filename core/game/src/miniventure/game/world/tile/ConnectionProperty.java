package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ConnectionProperty implements TilePropertyInstance {
	
	private final boolean connects;
	private final Array<TileType> connectingTiles;
	
	private final TileType tileType;
	
	ConnectionProperty(@NotNull TileType tileType, boolean connects, TileType... connectingTiles) {
		this.tileType = tileType;
		this.connects = connects;
		this.connectingTiles = new Array<>(connectingTiles);
		
		if(!this.connectingTiles.contains(tileType, true))
			this.connectingTiles.add(tileType);
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
					if(tile.getProp(aroundTypes[i][ti], TilePropertyType.Render).isOpaque()) // the type also doesn't connect, at this point.
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
		
		return tile.getProp(tileType, TilePropertyType.Render).getSprite(spriteIdx, false, tile);
	}
	
}
