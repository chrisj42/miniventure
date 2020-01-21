package miniventure.game.item;

import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ServerItemStack extends ItemStack {
	
	@NotNull public final ServerItem item;
	
	public ServerItemStack(@NotNull ServerItem item, int count) {
		super(item, count);
		this.item = item;
	}
	
	@NotNull
	@Override
	public ServerItem getItem() {
		return item;
	}
	
	public String[] save() { return save(item, count); }
	
	public static String[] save(@NotNull ServerItem item, int count) {
		return encodeStack(item.save(), count);
	}
	
	@NotNull
	public static ServerItemStack load(@NotNull String[] data, @NotNull Version version) {
		//noinspection ConstantConditions
		return new ServerItemStack(ServerItem.load(ItemStack.fetchItemData(data), version), ItemStack.fetchCount(data));
	}
	
	@Override
	public String toString() { return "Server"+super.toString(); }
	
}
