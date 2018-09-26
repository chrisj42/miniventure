package miniventure.game.item;

import java.awt.Color;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.MenuScreen;

public class InventoryScreen extends MenuScreen {
	
	private static final Color backgroundColor = new Color(0, 128, 128);
	
	private final Inventory inventory;
	private final ClientHands hands;
	
	public InventoryScreen(Inventory inventory, ClientHands hands) {
		super(true, false);
		this.inventory = inventory;
		this.hands = hands;
		
		// FIXME redo the item selection table entirely.
		
		
	}
	
	@Override
	public void focus() {
		ClientCore.setScreen(null);
	}
}
