package miniventure.game.screen;

import miniventure.game.client.ClientCore;

public class InfoScreen extends BackgroundInheritor {
	
	private InfoScreen(String... text) {
		addComponent(makeLabel("<p>"+String.join("</p><p>", text)+"</p>"));
		
		addComponent(30, makeButton("Back to Main Menu", ClientCore::backToParentScreen));
	}
	
	public static class InstructionsScreen extends InfoScreen {
		public InstructionsScreen() {
			super("Use mouse or arrow keys to move around.",
				"C to attack, V to interact.",
				"E to open your inventory, Z to craft items.",
				"Q to drop an item from your inventory/hotbar. Use Shift-Q to drop all items of that type.",
				"1,2,3 keys (or click) to select hotbar items.",
				"With the inventory open, click or press enter on an inventory item to swap it with the selected hotbar item.",
				"+ and - keys to zoom in and out.",
				"Press \"t\" to chat with other players, and \"/\" to use commands.",
				"(Hint: use the up key to repeat messages, and tab to autocomplete command names.)",
				"",
				"Toggle Debug display: Shift-D");
		}
	}
	
	public static class CreditsScreen extends InfoScreen {
		public CreditsScreen() {
			super(
				"Lead Developer: Chris J",
				"Music made by TrstN",
				"Sprites made by RiverOaken and MadDest"
			);
		}
	}
}
