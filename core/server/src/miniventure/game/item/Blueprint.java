package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

public class Blueprint extends Recipe {
	
	public Blueprint(@NotNull TileItemType.TileItem result, @NotNull ServerItemStack... costs) {
		super(result, costs);
	}
	
	@Override
	public String toString() {
		return getResult()+" Blueprint";
	}
}
