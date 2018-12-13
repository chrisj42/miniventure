package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class MutableItemStack {
	
	@NotNull
	private Item item;
	private int count;
	
	public MutableItemStack(@NotNull ItemStack stack) { this(stack.item, stack.count); }
	public MutableItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
	
	@NotNull
	public Item getItem() {
		return item;
	}
	
	public void setItem(@NotNull Item item) {
		this.item = item;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public String[] serialize() { return ItemStack.serialize(item, count); }
	
	@Override
	public String toString() { return "MutableItemStack("+count+' '+item+')'; }
}
