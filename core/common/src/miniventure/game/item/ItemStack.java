package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.network.GameProtocol.SerialItem;
import miniventure.game.network.GameProtocol.SerialItemStack;

import org.jetbrains.annotations.NotNull;

public class ItemStack {
	
	@NotNull public final Item item;
	public final int count;
	
	public ItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	@NotNull
	public Item getItem() { return item; }
	
	public static int fetchCount(String[] data) {
		return Integer.parseInt(data[0]);
	}
	
	public static String[] fetchItemData(String[] data) {
		return Arrays.copyOfRange(data, 1, data.length);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof ItemStack)) return false;
		ItemStack itemStack = (ItemStack) o;
		return count == itemStack.count && item.equals(itemStack.item);
	}
	
	@Override
	public int hashCode() {
		return count * 51 + item.hashCode() * 31;
	}
	
	@Override
	public String toString() { return "ItemStack("+count+' '+item+')'; }
}
