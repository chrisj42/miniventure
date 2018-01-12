package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class ConnectionProperty implements TileProperty {
	
	private final boolean connects;
	private final Array<TileType> connectingTiles;
	
	ConnectionProperty(boolean connects, TileType... connectingTiles) {
		this.connects = connects;
		this.connectingTiles = new Array<>(connectingTiles);
	}
	
	@Override
	public void init(TileType type) { addConnectingType(type); }
	
	void addConnectingType(TileType type) {
		if(!connectingTiles.contains(type, true))
			connectingTiles.add(type);
	}
	
	AtlasRegion getSprite(Tile tile, boolean useSurface) {
		int spriteIdx = 0;
		
		if(connects) {
			TileType[] aroundTiles = new TileType[9];
			int i = 0;
			for(int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
					if(oTile != null) {
						if(useSurface) aroundTiles[i] = oTile.getSurfaceType();
						if(aroundTiles[i] == null) // surface == false or no surface tile exists
							aroundTiles[i] = oTile.getGroundType();
					}
					i++;
				}
			}
		
			for (i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
				if (TileTouchCheck.connectionChecks[i].checkMatch(aroundTiles, aroundTiles[aroundTiles.length/2].getProp(ConnectionProperty.class).connectingTiles, null, false)) {
					spriteIdx = i;
					break;
				}
			}
		}
		
		TileType surfaceType = tile.getSurfaceType();
		TileType connectType = useSurface && surfaceType != null ? surfaceType : tile.getGroundType();
		return connectType.getProp(AnimationProperty.class).getSprite(spriteIdx, false, tile);
	}
	
	@Override
	public Integer[] getInitData() {
		return new Integer[0];
	}
}
