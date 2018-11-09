package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

final class HandItem extends ServerItem {
	
	// this is to be used only for the purposes of interaction; the inventory should not have them, and the hotbar ought to be filled with nulls for empty spaces.
	
	public static final HandItem hand = new HandItem();
	
	private HandItem() {
		super(ItemType.Misc, "Hand", GameCore.icons.get("blank"));
	}
	
	@Override
	public int getSpaceUsage() { return 0; }
	
	@Override
	public String[] save() {
		return new String[] {
			ItemType.Misc.name(),
			MyUtils.encodeStringArray(miniventure.game.item.HandItem.class.getSimpleName())
		};
	}
	
	/** @noinspection Contract*/
	public static ServerItem load(String[] data) { return hand; }
	
	@Override public ServerItem getUsedItem() { return this; }
	@Override public ServerItem copy() { return this; }
	
	@Override public boolean interact(WorldObject obj, Player player) {
		boolean success = obj.interactWith(player, null);
		if(success) use();
		return success;
	}
	@Override public boolean attack(WorldObject obj, Player player) {
		boolean success = obj.attackedBy(player, null, 1);
		if(success) use();
		return success;
	}
}
