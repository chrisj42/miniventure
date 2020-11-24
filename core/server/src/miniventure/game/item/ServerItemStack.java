package miniventure.game.item;

import java.util.Arrays;

import miniventure.game.network.GameProtocol.SerialItemStack;
import miniventure.game.util.Version;

import org.jetbrains.annotations.NotNull;

public class ServerItemStack extends ItemStack implements ServerItemStackSource {
	
	@NotNull public final ServerItem item;
	
	public ServerItemStack(@NotNull ServerItem item, int count) {
		super(item, count);
		this.item = item;
	}
	
	@Override @NotNull
	public ServerItemStack getStack() {
		return this;
	}
	
	@Override @NotNull
	public ServerItem getItem() {
		return item;
	}
	
	public SerialItemStack serialize() {
		return new SerialItemStack(item.serialize(), count);
	}
	
	public String[] save() { return save(item, count); }
	
	public static String[] save(@NotNull ServerItem item, int count) {
		String[] itemData = item.getSaveData();
		
		String[] data = new String[itemData.length+1];
		System.arraycopy(itemData, 0, data, 1, itemData.length);
		data[0] = String.valueOf(count);
		
		return data;
	}
	
	@NotNull
	public static ServerItemStack load(@NotNull String[] data, @NotNull Version version) {
		return new ServerItemStack(
			ServerItem.load(Arrays.copyOfRange(data, 1, data.length), version),
			Integer.parseInt(data[0])
		);
	}
	
	@Override
	public String toString() { return "Server"+super.toString(); }
	
}
