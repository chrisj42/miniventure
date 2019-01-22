package miniventure.game.screen;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class InfoScreen extends BackgroundInheritor {
	
	private InfoScreen(String... text) { this(false, text); }
	private InfoScreen(boolean addButton, String... text) {
		super(new ScreenViewport());
		
		VerticalGroup vGroup = useVGroup(30);
		
		vGroup.addActor(makeLabel(String.join("\n", text)));
		
		if(addButton)
			vGroup.addActor(makeButton("Start Game", () -> ClientCore.getWorld().createWorld(GameCore.DEFAULT_WORLD_SIZE, GameCore.DEFAULT_WORLD_SIZE)));
		
		vGroup.addActor(makeButton("Back to Main Menu", ClientCore::backToParentScreen));
	}
	
	public static class InstructionsScreen extends InfoScreen {
		public InstructionsScreen() { this(false); }
		public InstructionsScreen(boolean addButton) {
			super(addButton, "Use mouse or arrow keys to move around.",
				"C to attack, V to interact and do other things.",
				"E to open your inventory, Z to craft items.",
				"Q to drop an item from your inventory/hotbar. Use Shift-Q to drop all items in the stack.",
				"1-5 keys to select hotbar items.",
				"Inventory Screen: to put an item in your hotbar, press the 1-5 key matching the hotbar slot,",
				"    with the inventory item you want to be there selected.",
				"Crafting Screen: Click or press enter to craft the selected item.",
				"+ and - keys to zoom in and out.",
				"Press \"t\" to chat with other players, and \"/\" to use commands.",
				"(Hint: use the up key to repeat messages, and tab to autocomplete command names.)",
				"",
				"Toggle Debug display: Shift-D");
		}
		
		@Override
		public void focus() {
			ClientCore.viewedInstructions = true;
		}
	}
	
	public static class CreditsScreen extends InfoScreen {
		public CreditsScreen() {
			super(
				"Lead Developer: Chris J",
				/*"Music made by TrstN",*/
				"Sprites made by RiverOaken and Ross, with contributions by Theta and MadDest",
				"Sound effects made by Chris J"
			);
		}
	}
}
