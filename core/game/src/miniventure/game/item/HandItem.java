package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;

public final class HandItem extends Item {
	HandItem() {
		super(ItemType.Misc, "Hand", GameCore.icons.get("blank"));
	}
	
	@Override
	public int getSpaceUsage() { return 0; }
	
	@Override
	public String[] save() { return new String[] { ItemType.Misc.name(), MyUtils.encodeStringArray(HandItem.class.getSimpleName()) }; }
	
	/** @noinspection Contract*/
	public static Item load(String[] data) { return new HandItem(); }
	
	@Override public Item getUsedItem() { return this; }
	@Override public Item copy() { return this; }
	
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
