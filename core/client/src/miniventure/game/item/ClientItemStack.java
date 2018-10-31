package miniventure.game.item;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

public class ClientItemStack extends ItemStack {
	public ClientItemStack(@NotNull Item item, int count) {
		super(item, count);
	}
	
	public static ClientItemStack load(String[] data) {
		int count = Integer.parseInt(data[0]);
		ClientItem item = ClientItem.load(Arrays.copyOfRange(data, 1, data.length));
		return new ClientItemStack(item, count);
	}
}
