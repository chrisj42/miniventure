package miniventure.game.item.type;

import miniventure.game.item.type.TileItem.PlacementFunction;
import miniventure.game.item.type.ToolItem.Material;
import miniventure.game.world.tile.TileType;

import org.jetbrains.annotations.NotNull;

public enum ItemType {
	
	Sword((it, d) -> new ToolItem(it, Material.values()[d])),
	Pickaxe((it, d) -> new ToolItem(it, Material.values()[d])),
	Shovel((it, d) -> new ToolItem(it, Material.values()[d])),
	Axe((it, d) -> new ToolItem(it, Material.values()[d])),
	Hoe((it, d) -> new ToolItem(it, Material.values()[d])),
	
	Torch((it, d) -> new TileItem(TileType.TORCH, PlacementFunction.LAND)),
	Sand((it, d) -> new TileItem(TileType.SAND, TileType.DIRT)),
	Grass((it, d) -> new TileItem(TileType.GRASS, TileType.DIRT)),
	Dirt((it, d) -> new TileItem(TileType.DIRT, TileType.HOLE)),
	
	Apple((it, d) -> new FoodItem(it.name(), 2)),
	
	// resource items; these don't really have any special properties.
	
	Log;
	
	
	interface ItemFetcher {
		Item get(ItemType it, int data);
	}
	
	@NotNull private final ItemFetcher fetcher;
	
	ItemType() { this((it, d) -> new ResourceItem(it.name())); }
	ItemType(@NotNull ItemFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	public Item getItem() { return fetcher.get(this, 0); }
	public Item getItem(int data) { return fetcher.get(this, data); }
	
}
