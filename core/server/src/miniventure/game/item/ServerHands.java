package miniventure.game.item;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.mob.Player.Stat;
import miniventure.game.world.entity.mob.ServerPlayer;

public class ServerHands extends Hands {
	
	private ServerPlayer player;
	
	public ServerHands(ServerPlayer player) {
		super(player.getInventory());
		this.player = player;
	}
	
	public void resetItemUsage() {
		Item item = getSelectedItem();
		
		if(!item.isUsed()) return;
		
		//System.out.println("used item "+item);
		Item newItem = item.resetUsage();
		
		if(!GameCore.debug)
			player.changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		
		// remove the current item, no matter what.
		getInv().removeItem(item);
		if(newItem != null) {
			// the item has changed, either in metadata or into an entirely separate item.
			// add the new item to the inventory, and then determine what should become the held item: previous or new item.
			// if the new item doesn't fit, then drop it on the ground instead.
			
			if(!getInv().addItem(newItem)) {
				// inventory is full, try to drop it on the ground
				ServerLevel level = player.getLevel();
				if(level != null)
					level.dropItem(newItem, player.getCenter(), player.getCenter().add(player.getDirection().getVector()));
				else // this is a very bad situation, ideally it should never happen.
					System.err.println("could not drop usage-spawn item "+newItem+", ServerLevel for player "+player+" is null. (inventory is also full)");
			}
			else {
				// item was successfully added to the inventory
				
				// check if the original item has run out, in which case the new item should replace it in the hotbar.
				if(!getInv().hasItem(item))
					replaceItem(item, newItem);
				else {
					// original item still exists; decide if new item should replace it or not
					if(newItem.getName().equals(item.getName())) {
						// metadata changed, same item, replace stack
						replaceItem(item, newItem);
						// try and keep the stack in the hotbar
						addItem(item, getSelection());
					}
					else // name changed, different item, keep stack
						addItem(newItem); // try-add new item to hotbar like when you normally pick items up
				}
			}
		}
		
		// remove old item from hotbar if it's no longer in the inventory
		if(!getInv().hasItem(item))
			removeItem(item);
		
		ServerCore.getServer().sendToPlayer(player, new InventoryUpdate(player));
	}
	
	@Override
	public boolean hasUsableItem() { return super.hasUsableItem() && !(player.getStat(Stat.Stamina) < getSelectedItem().getStaminaUsage()); }
}
