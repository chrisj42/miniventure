package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class Blueprint extends Recipe {
	
	public Blueprint(@NotNull TileItem result, @NotNull ServerItemStack... costs) {
		super(result, costs);
	}
	
}
