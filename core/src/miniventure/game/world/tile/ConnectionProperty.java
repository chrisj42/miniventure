package miniventure.game.world.tile;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class ConnectionProperty implements TileProperty {
	
	private final boolean connects;
	
	ConnectionProperty(boolean connects) {
		this.connects = connects;
	}
	
	AtlasRegion getSprite(Tile tile) {
		return getSprite(tile, false);
	}
	AtlasRegion getSprite(Tile tile, boolean under) {
		
		int spriteIdx = 0;
		if(connects) {
			TileType[] aroundTiles = new TileType[9];
			int i = 0;
			for(int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					Tile oTile = tile.getLevel().getTile(tile.x + x, tile.y + y);
					if(oTile != null) {
						TileType oUnder = oTile.getType().animationProperty.renderBehind;
						if(under && oUnder != null)
							aroundTiles[i] = oUnder;
						else
							aroundTiles[i] = oTile.getType();
					}
					i++;
				}
			}
		
			for (i = 0; i < TileTouchCheck.connectionChecks.length; i++) {
				if (TileTouchCheck.connectionChecks[i].checkMatch(aroundTiles, aroundTiles[aroundTiles.length/2], null, false)) {
					spriteIdx = i;
					break;
				}
			}
		}
		
		TileType curUnder = tile.getType().animationProperty.renderBehind;
		TileType type = under && curUnder != null ? curUnder : tile.getType();
		return type.animationProperty.getSprite(spriteIdx, false, tile, type);
	}
	
	@Override
	public Integer[] getInitData() {
		return new Integer[0];
	}
}
