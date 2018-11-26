package miniventure.game.item;

import java.util.Arrays;
import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.tile.ServerTile;
import miniventure.game.world.tile.ServerTileType;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileItem extends ServerItem {
	
	private static final EnumMap<TileTypeEnum, TileItem> items = new EnumMap<>(TileTypeEnum.class);
	
	private static final TileTypeEnum[] groundTypes = new TileTypeEnum[] {
		TileTypeEnum.GRASS, TileTypeEnum.DIRT, TileTypeEnum.SAND, TileTypeEnum.SNOW, TileTypeEnum.STONE_FLOOR, TileTypeEnum.STONE_PATH
	};
	
	private static void addItem(TileTypeEnum result, TileTypeEnum... canPlaceOn) {
		items.put(result, new TileItem(result, canPlaceOn));
	}
	
	static {
		// for example, acorns.
		// one improvement, btw, could be that in the current system, there can only be one item per tile. You can't have two items that end up producing the same tile. You even search by TileTypeEnum. I don't necessarily like this...
		addItem(TileTypeEnum.TORCH, groundTypes);
		
		addItem(TileTypeEnum.DIRT, TileTypeEnum.HOLE);
		addItem(TileTypeEnum.SAND, TileTypeEnum.DIRT);
		addItem(TileTypeEnum.GRASS, TileTypeEnum.DIRT);
		addItem(TileTypeEnum.STONE, TileTypeEnum.DIRT);
		
		items.put(TileTypeEnum.CLOSED_DOOR, new TileItem("Door", GameCore.tileAtlas.findRegion("closed_door/c00"), TileTypeEnum.CLOSED_DOOR, groundTypes));
		items.put(TileTypeEnum.OPEN_DOOR, items.get(TileTypeEnum.CLOSED_DOOR));
		
		items.put(TileTypeEnum.DIRT, new TileItem("Dirt", GameCore.icons.get("items/dirt"), TileTypeEnum.DIRT, TileTypeEnum.HOLE));
		
		addItem(TileTypeEnum.STONE_FLOOR, TileTypeEnum.HOLE/*, TileTypeEnum.DIRT*/);
		
		addItem(TileTypeEnum.STONE_PATH, TileTypeEnum.DIRT, TileTypeEnum.SAND, TileTypeEnum.GRASS, TileTypeEnum.SNOW);
	}
	
	@NotNull
	public static TileItem get(@NotNull TileTypeEnum tile) {
		if(!items.containsKey(tile))
			items.put(tile, new TileItem(tile, groundTypes));
		return items.get(tile).copy();
	}
	
	@NotNull private final TileTypeEnum result;
	@Nullable private final TileTypeEnum[] canPlaceOn;
	
	private TileItem(@NotNull TileTypeEnum type, @Nullable TileTypeEnum... canPlaceOn) {
		this(MyUtils.toTitleCase(type.name()), GameCore.tileAtlas.findRegion(type.name().toLowerCase()+"/c00"), type, canPlaceOn); // so, if the placeOn is null, then...
	}
	
	private TileItem(String name, TextureHolder texture, @NotNull TileTypeEnum result, @Nullable TileTypeEnum... placeOn) {
		super(ItemType.Tile, name, texture);
		this.canPlaceOn = placeOn;
		this.result = result;
	}
	
	@Override
	public Result interact(WorldObject obj, Player player) {
		if(obj instanceof ServerTile) {
			ServerTile tile = (ServerTile) obj;
			boolean canPlace = canPlaceOn == null;
			if(!canPlace) {
				for (TileTypeEnum underType : canPlaceOn) {
					if(underType == tile.getType().getTypeEnum()) {
						canPlace = true;
						break;
					}
				}
			}
			
			if(canPlace && tile.addTile(ServerTileType.get(result))) {
				return Result.USED;
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
