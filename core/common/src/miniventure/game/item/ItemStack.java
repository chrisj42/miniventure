package miniventure.game.item;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStack {
	
	@NotNull public final Item item;
	public final int count;
	
	public ItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public String[] serialize() { return serialize(item, count); }
	
	public static String[] serialize(@NotNull Item item, int count) {
		String[] itemData = item.serialize();
		String[] data = new String[itemData.length+1];
		System.arraycopy(itemData, 0, data, 1, itemData.length);
		data[0] = String.valueOf(count);
		
		return data;
	}
	
	@NotNull
	public static ItemStack deserialize(@NotNull String[] data) {
		int count = Integer.parseInt(data[0]);
		Item item = Item.deserialize(Arrays.copyOfRange(data, 1, data.length));
		return new ItemStack(item, count);
	}
	
	@Override
	public String toString() { return "ItemStack("+count+' '+item+')'; }
}
