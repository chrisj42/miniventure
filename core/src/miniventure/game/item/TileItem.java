package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

public class TileItem extends Item {
	
	private TileType tileType;
	
	public TileItem(TileType tile) {
		super(GameCore.tileAtlas.findRegion(tile.name().toLowerCase()+"/00"));
		this.tileType = tile;
	}
	
	@Override
	public String getName() { return tileType.name(); }
	
	@Override
	public boolean interact(WorldObject obj, Player player) {
		if(isUsed()) return true;
		
		if(obj instanceof Tile) {
			Tile tile = (Tile) obj;
			if (tileType.destructibleProperty.getCoveredTile() == tile.getType()) {
				tile.resetTile(tileType);
				setUsed();
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Item clone() {
		return new TileItem(tileType);
	}
}
