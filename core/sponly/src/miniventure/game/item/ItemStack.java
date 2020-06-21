package miniventure.game.item;

import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ItemStack {
	
	@NotNull
	public final Item item;
	public final int count;
	
	public ItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	public ItemStack(String data, Version version) {
		String countData = data.substring(0, data.indexOf(','));
		count = Integer.parseInt(countData);
		String itemData = data.substring(countData.length()+1);
		item = Item.load(itemData, version);
	}
	
	public static String save(@NotNull Item item, int count) {
		return count+","+item.save();
	}
	
	public String save() { return save(item, count); }
	
	@NotNull
	public Item getItem() { return item; }
	public int getCount() { return count; }
	
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
