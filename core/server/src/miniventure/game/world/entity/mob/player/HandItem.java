package miniventure.game.world.entity.mob.player;

import miniventure.game.GameCore;
import miniventure.game.item.ItemType.EphemeralItem;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player.CursorHighlight;

import org.jetbrains.annotations.NotNull;

public final class HandItem extends EphemeralItem {
	
	// this is to be used only for the purposes of interaction; the inventory should not have them, and the hotbar ought to be filled with nulls for empty spaces.
	
	public static final HandItem hand = new HandItem();
	
	private HandItem() {
		super("Hand", GameCore.icons.get("blank"));
	}
	
	@Override @NotNull
	public Player.CursorHighlight getHighlightMode() {
		return CursorHighlight.TILE_ADJACENT;
	}
	
	@Override public ServerItem getUsedItem() { return this; }
	
	@Override public Result interact(WorldObject obj, ServerPlayer player) {
		return obj.interactWith(player, null);
	}
	@Override public Result attack(WorldObject obj, ServerPlayer player) {
		return obj.attackedBy(player, null, 1);
	}
}
