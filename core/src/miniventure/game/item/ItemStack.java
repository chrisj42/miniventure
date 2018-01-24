package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class ItemStack {
	
	@NotNull public final Item item;
	public final int count;
	
	ItemStack(@NotNull Item item, int count) {
		this.item = item;
		this.count = count;
	}
}
