package miniventure.game.item;

public class ItemStack {
	
	public final Item item;
	public final int count;
	
	public ItemStack(Item item, int count) {
		this.item = item;
		this.count = count;
	}
}
