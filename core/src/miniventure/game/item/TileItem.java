package miniventure.game.item;

import java.util.HashMap;

import miniventure.game.screen.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.CoveredTileProperty;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public class TileItem extends ItemData {
	
	private static final HashMap<TileType, TileItem> items = new HashMap<>();
	
	static {
		// here, I should put all the tile items that I want to have custom info, not directly fetched from the tile.
		// for example, acorns.
		// one improvement, btw, could be that in the current system, there can only be one item per tile. You can't have two items that end up producing the same tile. You even search by TileType.
	}
	
	public static TileItem get(TileType tile) {
		if(tile == null) return null;
		if(!items.containsKey(tile))
			items.put(tile, new TileItem(tile));
		return items.get(tile);
	}
	
	private TileType placeOn, result;
	
	private TileItem(@NotNull TileType type) {
		this(MyUtils.toTitleCase(type.name()), GameCore.tileAtlas.findRegion(type.name().toLowerCase()+"/00"), type, type.getProp(CoveredTileProperty.class).getCoveredTile()); // so, if the placeOn is null, then...
	}
	
	private TileItem(String name, TextureRegion texture, TileType result, TileType placeOn) {
		super(name, texture);
		this.placeOn = placeOn;
		this.result = result;
	}
	
	@Override
	public boolean interact(Item item, WorldObject obj, Player player) {
		if(item.isUsed()) return true;
		
		if(obj instanceof Tile) {
			Tile tile = (Tile) obj;
			if (placeOn == tile.getType()) {
				tile.resetTile(result);
				item.setUsed();
				return true;
			}
		}
		
		return false;
	}
}
