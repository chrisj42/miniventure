package miniventure.game.item;

import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.server.ServerCore;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.particle.ItemEntity;

import org.jetbrains.annotations.NotNull;

public class ServerHands extends Hands {
	
	public ServerHands(Player player) {
		super(player);
	}
	
	@Override
	void dropStack(@NotNull ItemStack stack) {
		int removed = player.getInventory().removeItem(stack.item, stack.count);
		
		ServerLevel level = (ServerLevel) player.getLevel();
		if(level != null)
			for(int i = 0; i < removed; i++)
				level.addEntity(new ItemEntity(player.getWorld(), stack.item, null), player.getPosition(), true);
		
		// TO-DO update the client's inventory (note that the server ultimately keeps track of the player's inventory)
		ServerCore.getServer().sendToPlayer(player, new InventoryUpdate(player));
	}
	
	@Override
	public void resetItemUsage() {
		super.resetItemUsage();
		
		ServerCore.getServer().sendToPlayer(player, new InventoryUpdate(player));
	}
}
