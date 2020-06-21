package miniventure.game.world.entity.mob.player;

import miniventure.game.item.Item;
import miniventure.game.item.ItemType.EphemeralItem;
import miniventure.game.item.Result;
import miniventure.game.texture.ItemTextureSource;
import miniventure.game.world.WorldObject;

import org.jetbrains.annotations.NotNull;

public final class HandItem extends EphemeralItem {
	
	// this is to be used only for the purposes of interaction; the inventory should not have them, and the hotbar ought to be filled with nulls for empty spaces.
	
	public static final HandItem hand = new HandItem();
	
	private HandItem() {
		super("Hand", ItemTextureSource.Icon_Map.get("blank"));
	}
	
	@Override @NotNull
	public CursorHighlight getHighlightMode() {
		return CursorHighlight.TILE_ADJACENT;
	}
	
	@Override public Item getUsedItem() { return this; }
	
	@Override public Result interact(WorldObject obj, Player player) {
		return obj.interactWith(player, null);
	}
	@Override public Result attack(WorldObject obj, Player player) {
		return obj.attackedBy(player, null, 1);
	}
}
