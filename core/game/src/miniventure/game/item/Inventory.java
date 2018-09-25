package miniventure.game.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import miniventure.game.util.InstanceCounter;
import miniventure.game.util.MyUtils;

public abstract class Inventory {
	
	private static final HandItem hand = new HandItem(); // ref var so it isn't constantly re-instantiated.
	
	private final LinkedHashMap<Item, Integer> items = new LinkedHashMap<>();
	
	public void reset() {
		items.clear();
	}
	
	public int getSize() { return items.size(); }
	public int getCount(Item item) { return items.getOrDefault(item, 0); }
	
	public Item[] getItems() { return items.keySet().toArray(new Item[0]); }
	
	public boolean hasItem(Item item) { return hasItem(item, 1); }
	public boolean hasItem(Item item, int count) { return getCount(item) >= count; }
	
	public boolean addItem(Item item) { return addItem(item, 1); }
	public boolean addItem(ItemStack itemStack) { return addItem(itemStack.item, itemStack.count); }
	public boolean addItem(Item item, int count) {
		if(!itemCounter.containsKey(item))
			items.add(item);
		
	}
	
	public boolean removeItem(Item item, boolean all) {
		if(!itemCounter.containsKey(item))
			return false;
		
		if(itemCounter.get(item) == 1 || all) {
			items.remove(item);
			itemCounter.remove(item);
			return true;
		}
		else {
			itemCounter.removeInstance(item);
			return true;
		}
	}
	
	Item getItemAt(int idx) { return items.get(idx); }
	
	Item replaceItemAt(int idx, ItemStack item) {
		
	}
	
	public String[] save() {
		String[] data = new String[getSize()];
		for(int i = 0; i < data.length; i++)
			data[i] = MyUtils.encodeStringArray(ItemStack.save(items.get(i), ));
		
		return data;
	}
	
	public void loadItems(String[] allData) {
		reset();
		for(String data: allData)
			loadItem(MyUtils.parseLayeredString(data));
	}
	
}
