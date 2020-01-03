package miniventure.game.item;

import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ServerInventory extends Inventory<ServerItem, ServerItemStack> {
	
	public ServerInventory(int size) {
		super(size, ServerItem.class, ServerItemStack.class);
	}
	
	public String[][] serialize() {
		String[][] data = new String[uniqueItems.size()][];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = ItemStack.serialize(item, itemCounter.get(item));
		}
		
		return data;
	}
	
	public String[] save() {
		String[] data = new String[uniqueItems.size()];
		for(int i = 0; i < data.length; i++) {
			ServerItem item = uniqueItems.get(i);
			data[i] = MyUtils.encodeStringArray(ServerItemStack.save(item, itemCounter.get(item)));
		}
		
		return data;
	}
	
	// this expects exactly the output of the save function above.
	public void loadItems(String[] allData, @NotNull Version version) { loadItems(allData, 0, version); }
	public void loadItems(String[] allData, int buffer, @NotNull Version version) {
		ServerItemStack[] stacks = new ServerItemStack[allData.length];
		
		for(int i = 0; i < allData.length; i++) {
			stacks[i] = ServerItemStack.load(MyUtils.parseLayeredString(allData[i]), version);
		}
		
		setItems(stacks, buffer);
	}
}
