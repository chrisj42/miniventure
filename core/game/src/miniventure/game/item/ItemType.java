package miniventure.game.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import miniventure.game.item.ToolItem.Material;
import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileType;

public enum ItemType {
	
	Tool(data -> new ToolItem(ToolType.valueOf(data[0]), Material.valueOf(data[1]), Integer.parseInt(data[2]))),
	
	Food(data -> FoodItem.valueOf(data[0]).get()),
	
	Tile(data -> TileItem.get(TileType.valueOf(data[0]))),
	
	Resource(data -> ResourceItem.valueOf(data[0]).get()),
	
	Misc(data -> {
		// The data all items has the item type as the first entry. This item type expects the next data string to be an encoded array of the enclosing classes needed to reach the class you're trying to reach, such as a HandItem. If the class is top-level, then the encoded array should just be the name of the class.
		// the static load method of the found class is then called with any remaining parameters from the original data array, past the first two.
		
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
