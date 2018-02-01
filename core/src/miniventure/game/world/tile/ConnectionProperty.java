package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class ConnectionProperty implements TileProperty {
	
	private final boolean connects;
	private final Array<TileType> connectingTiles;
	
	private TileType tileType;
	
	ConnectionProperty(boolean connects, TileType... connectingTiles) {
		this.connects = connects;
		this.connectingTiles = new Array<>(connectingTiles);
	}
	
	@Override
	public void init(TileType type) {
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
			/*TileType[] aroundTiles = new TileType[9];
			int i = 0;
			for(int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
					if(oTile != null) {
						if(!tileType.isGroundTile()) aroundTiles[i] = oTile.getSurfaceType();
						if(aroundTiles[i] == null) // surface == false or no surface tile exists
							aroundTiles[i] = oTile.getGroundType();
					}
					i++;
				}
			}*/
			
			/*Array<Boolean[]> connectingTypes = new Array<>();
			
			Iterator<TileType> aroundIter = aroundTypes.descendingKeySet().iterator();
			TileType first = aroundIter.next();
			while(aroundIter.hasNext() && !first.getProp(AnimationProperty.class).isOpaque())
				first = aroundIter.next();
			for(TileType type: aroundTypes.tailMap(first, true).keySet())
				if(connectingTiles.contains(type, true))
					connectingTypes.add(aroundTypes.get(type));
			*/
			
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
}
