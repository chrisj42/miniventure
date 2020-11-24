package miniventure.game.item;

import miniventure.game.item.ItemType.EnumItem;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public enum ResourceType implements ServerItemSource {
	
	Stick, Plank,
	Reed,
	Flint, Coal, Stone,
	Iron, Tungsten, Ruby,
	Cotton, Fabric,
	;
	
	@NotNull
	private final ServerItem item;
	
	ResourceType() {
		item = new EnumItem(ItemType.Resource, this) {
			@Override @NotNull
			public Player.CursorHighlight getHighlightMode() {
				return CursorHighlight.INVISIBLE;
			}
		};
	}
	
	@Override @NotNull
	public ServerItem get() {
		return item;
	}
	
}
