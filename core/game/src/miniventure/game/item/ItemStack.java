package miniventure.game.item;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

public class ItemStack {
	
	@NotNull public final Item item;
	public final int count;
	
	ItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public static String[] save(Item item, int count) {
		String[] itemData = item.save();
		String[] data = new String[itemData.length+1];
		System.arraycopy(itemData, 0, data, 1, itemData.length);
		data[0] = count+"";
		
		return data;
	}
	
	public static ItemStack load(String[] data) {
		int count = Integer.parseInt(data[0]);
		Item item = Item.load(Arrays.copyOfRange(data, 1, data.length));
		return new ItemStack(item, count);
	}
}
