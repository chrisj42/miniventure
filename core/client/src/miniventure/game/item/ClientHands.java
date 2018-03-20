package miniventure.game.item;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.NotNull;

public class ClientHands extends Hands {
	
	public ClientHands(Player player) {
		super(player);
	}
	
	@Override
	void dropStack(@NotNull ItemStack stack) {
		ClientCore.getClient().send(new ItemDropRequest(stack));
	}
}
