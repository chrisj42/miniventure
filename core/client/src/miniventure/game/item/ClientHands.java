package miniventure.game.item;

import miniventure.game.GameProtocol.ItemDropRequest;
import miniventure.game.client.ClientCore;

public class ClientHands extends Hands {
	
	public ClientHands(Inventory inventory) {
		super(inventory);
	}
	
	public void dropInvItems(Item item, boolean all) {
		if(!(item instanceof HandItem)) {
			int count = all ? getInv().getCount(item) : 1;
			for(int i = 0; i < count; i++)
				getInv().removeItem(item);
			ClientCore.getClient().send(new ItemDropRequest(new ItemStack(item, count)));
		}
	}
}
