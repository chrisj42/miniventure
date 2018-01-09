package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

public class TileItem extends Item {
	
	private TileType tileType;
	
	public TileItem(TileType tile) {
		super(GameCore.icons.get("gem-sword"/*tile.name().toLowerCase()*/));
		this.tileType = tile;
	}
	
	@Override
	public String getName() {
		return tileType.name();
	}
	
	@Override public boolean isReflexive() { return false; }
	
	@Override
	public boolean interact(Player player, Tile tile) {
		if(tile != null && tileType.destructibleProperty.getCoveredTile() == tile.getType()) {
			tile.resetTile(tileType);
			setUsed();
			return true;
		}
		
		return false;
	}
	
	@Override
	public Item clone() {
		return new TileItem(tileType);
	}
}
