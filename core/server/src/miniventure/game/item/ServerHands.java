package miniventure.game.item;

import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.world.entity.mob.Player.Stat;
import miniventure.game.world.entity.mob.ServerPlayer;

public class ServerHands extends Hands {
	
	private ServerPlayer player;
	
	public ServerHands(ServerPlayer player) {
		super(player.getInventory());
		this.player = player;
	}
	
	/*@Override
	void dropItem(@NotNull Item item) {
		boolean removed = player.getInventory().removeItem(item);
		
		ServerLevel level = player.getLevel();
		if(level != null && removed)
			level.addEntity(new ItemEntity(item, player.getDirection().getVector()), player.getPosition(), true);
		
		// TO-DO update the client's inventory (note that the server ultimately keeps track of the player's inventory)
		ServerCore.getServer().sendToPlayer(player, new InventoryUpdate(player.getInventory(), player.getHands()));
	}*/
	
	@Override
	public void resetItemUsage() {
		super.resetItemUsage();
		
		Item item = getSelectedItem();
		
		if(!item.isUsed()) return;
		
		//System.out.println("used item "+item);
		Item newItem = item.resetUsage();
		
		player.changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		/*if(getSelectedItemCount() == 1 || item instanceof HandItem)
			setItem(newItem == null ? new HandItem() : newItem, 1);
		else {
			setItem(item, getSelectedItemCount()-1);
			if(newItem != null && !player.takeItem(newItem)) {// will add it to hand, or inventory, whichever fits.
				// this happens if the inventory is full; in such a case, drop the new item on the ground.
				dropItem(newItem); // if there was a new item, and it couldn't be picked up, then the count is not decreased.
			}
		}*/
		replaceItemAt(getSelection(), newItem);
		
		ServerCore.getServer().sendToPlayer(player, new InventoryUpdate(player.getInventory(), player.getHands()));
	}
	
	@Override
	public boolean hasUsableItem() { return super.hasUsableItem() && !(player.getStat(Stat.Stamina) < getSelectedItem().getStaminaUsage()); }
}
