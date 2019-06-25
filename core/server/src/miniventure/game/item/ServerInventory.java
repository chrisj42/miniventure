package miniventure.game.item;

import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ServerInventory extends Inventory<ServerItem, ServerItemStack> {
	
	public ServerInventory(int size) {
		super(size, ServerItem.class, ServerItemStack.class);
	}
	
	public synchronized String[][] serialize() {
		String[][] data = new String[uniqueItems.size()][];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = ItemStack.serialize(item, itemCounter.get(item));
		}
		
		return data;
	}
	
	public synchronized String[] save() {
		String[] data = new String[uniqueItems.size()];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = MyUtils.encodeStringArray(ServerItemStack.save(item, itemCounter.get(item)));
		}
		
		return data;
	}
	
	// this expects exactly the output of the save function above.
	public synchronized void loadItems(String[] allData, @NotNull Version version) {
		String[][] longData = new String[allData.length][];
		
		for(int i = 0; i < allData.length; i++) {
			longData[i] = MyUtils.parseLayeredString(allData[i]);
		}
		
		updateItems(longData, version);
	}
	
	@Override
	ServerItemStack parseStack(String[] data, @NotNull Version version) {
		return ServerItemStack.load(data, version);
	}
}
