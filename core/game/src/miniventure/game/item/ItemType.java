package miniventure.game.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileType;

public enum ItemType {
	
	Tool(data -> new ToolItem(ToolType.valueOf(data[0]), Material.valueOf(data[1]), Integer.parseInt(data[2]))),
	
	Food(data -> FoodItem.valueOf(data[0]).get()),
	
	Tile(data -> TileItem.get(TileType.valueOf(data[0]))),
	
	Resource(data -> ResourceItem.valueOf(data[0]).get()),
	
	Misc(data -> {
		try {
			String[] enclosingClasses = MyUtils.parseLayeredString(data[0]);
			Class<?> clazz = Class.forName(ItemType.class.getPackage().getName()+"."+enclosingClasses[0]);
			for(int i = 1; i < enclosingClasses.length; i++) {
				for(Class<?> inner: clazz.getDeclaredClasses()) {
					if(inner.getSimpleName().equals(enclosingClasses[i])) {
						clazz = inner;
						break;
					}
				}
			}
			return (Item) clazz.getMethod("load", String[].class).invoke(null, (Object)Arrays.copyOfRange(data, 1, data.length));
		} catch(ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return null;
	});
	
	/*
		This is mainly for saving and loading.
		Each item type should specify a method to turn a String into an Item, and an item into a String.
		There should also be some external connection, or something, so that you can fetch the ItemType from the item.
		
		instances of the item class require an ItemType instance in the constructor.
	 */
	
	@FunctionalInterface
	interface ItemFetcher {
		Item load(String[] data);
	}
	
	private ItemFetcher fetcher;
	ItemType(ItemFetcher fetcher) {
		this.fetcher = fetcher;
	}
	
	public Item load(String[] data) {
		return fetcher.load(data);
	}
}
