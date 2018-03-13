package miniventure.game.item.typeold;

import miniventure.game.GameCore;
import miniventure.game.item.type.Item;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public class TileItem extends Item {
	
	@FunctionalInterface
	interface PlacementFunction {
		boolean canPlace(TileType type);
		
		PlacementFunction ANY = (type) -> true;
		PlacementFunction LAND = oneOf(TileType.GRASS, TileType.DIRT, TileType.SAND);
		
		static PlacementFunction oneOf(TileType... types) {
			return (type) -> {
				for(TileType listType: types)
					if(listType == type)
						return true;
				
				return false;
			};
		}
	}
	
	/*private static final HashMap<TileType, TileItem> items = new HashMap<>();
	
	static {
		// TO-DO here, I should put all the tile items that I want to have custom info, not directly fetched from the tile.
		// for example, acorns.
		// one improvement, btw, could be that in the current system, there can only be one item per tile. You can't have two items that end up producing the same tile. You even search by TileType.
		
		items.put(TileType.DOOR_CLOSED, new TileItem("Door", GameCore.tileAtlas.findRegion("door_closed/00"), TileType.DOOR_CLOSED, PlacementFunction.LAND));
		items.put(TileType.DOOR_OPEN, items.get(TileType.DOOR_CLOSED));
	}
	
	@NotNull
	public static TileItem get(@NotNull TileType tile) {
		if(!items.containsKey(tile))
			items.put(tile, new TileItem(tile, *//* here, it will be in the item type. *//*));
		return items.get(tile).copy();
	}*/
	
	@NotNull private TileType result;
	@NotNull private PlacementFunction placer;
	
	public TileItem(@NotNull TileType type, @NotNull PlacementFunction placer) {
		this(MyUtils.toTitleCase(type.name()), GameCore.tileAtlas.findRegion(type.name().toLowerCase()+"/00"), type, placer); // so, if the placeOn is null, then...
	}
	
	private TileItem(String name, TextureRegion texture, @NotNull TileType result, @NotNull PlacementFunction placer) {
		super(name, texture);
		this.placer = placer;
		this.result = result;
	}
	
	@Override
	public boolean interact(WorldObject obj, Player player) {
		if(!isUsed() && obj instanceof Tile) {
			Tile tile = (Tile) obj;
			
			if(placer.canPlace(tile.getType()) && tile.addTile(result)) {
				use();
				return true;
			}
		}
		
		return obj.interactWith(player, this);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!super.equals(other)) return false;
		TileItem ti = (TileItem) other;
		return ti.result == result;
	}
	
	@Override
	public int hashCode() { return super.hashCode() - 17 * result.hashCode(); }
	
	@Override
	public TileItem copy() {
		return new TileItem(getName(), getTexture(), result, placer);
	}
}
