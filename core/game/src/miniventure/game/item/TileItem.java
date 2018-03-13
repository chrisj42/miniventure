package miniventure.game.item;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.CoveredTileProperty;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileItem extends Item {
	
	private static final HashMap<TileType, TileItem> items = new HashMap<>();
	
	private static final TileType[] groundTypes = new TileType[] {
		TileType.GRASS, TileType.DIRT, TileType.SAND
	};
	
	private static void addItem(TileType result, TileType... canPlaceOn) {
		items.put(result, new TileItem(result, canPlaceOn));
	}
	
	static {
		// for example, acorns.
		// one improvement, btw, could be that in the current system, there can only be one item per tile. You can't have two items that end up producing the same tile. You even search by TileType. I don't necessarily like this...
		addItem(TileType.TORCH, groundTypes);
		
		addItem(TileType.DIRT, TileType.HOLE);
		addItem(TileType.SAND, TileType.DIRT);
		addItem(TileType.GRASS, TileType.DIRT);
		addItem(TileType.STONE, TileType.DIRT);
		
		items.put(TileType.DOOR_CLOSED, new TileItem("Door", GameCore.tileAtlas.findRegion("door_closed/00"), TileType.DOOR_CLOSED, groundTypes));
		items.put(TileType.DOOR_OPEN, items.get(TileType.DOOR_CLOSED));
	}
	
	@NotNull
	public static TileItem get(@NotNull TileType tile) {
		if(!items.containsKey(tile))
			items.put(tile, new TileItem(tile, groundTypes));
		return items.get(tile).copy();
	}
	
	@NotNull private TileType result;
	@Nullable private TileType[] canPlaceOn;
	
	private TileItem(@NotNull TileType type, @Nullable TileType... canPlaceOn) {
		this(MyUtils.toTitleCase(type.name()), GameCore.tileAtlas.findRegion(type.name().toLowerCase()+"/00"), type, canPlaceOn); // so, if the placeOn is null, then...
	}
	
	private TileItem(String name, TextureRegion texture, @NotNull TileType result, @Nullable TileType... placeOn) {
		super(ItemType.Tile, name, texture);
		this.canPlaceOn = placeOn;
		this.result = result;
	}
	
	@Override
	public boolean interact(WorldObject obj, Player player) {
		if(!isUsed() && obj instanceof Tile) {
			Tile tile = (Tile) obj;
			boolean canPlace = canPlaceOn == null;
			if(!canPlace) {
				for (TileType underType : canPlaceOn) {
					if(underType == tile.getType()) {
						canPlace = true;
						break;
					}
				}
			}
			
			if(canPlace && tile.addTile(result)) {
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
		return Arrays.equals(canPlaceOn, ti.canPlaceOn) && ti.result == result;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() - 17 * Arrays.hashCode(canPlaceOn) + result.hashCode();
	}
	
	@Override
	public TileItem copy() {
		return new TileItem(getName(), getTexture(), result, canPlaceOn);
	}
	
	@Override
	public String[] save() {
		return new String[] {getType().name(), result.name()};
	}
}
