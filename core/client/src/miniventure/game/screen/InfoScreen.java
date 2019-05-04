package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class InfoScreen extends BackgroundInheritor {
	
	private InfoScreen(String... text) { this(false, text); }
	private InfoScreen(boolean addButton, String... text) {
		super(new ScreenViewport());
		
		// VerticalGroup vGroup = useVGroup(addButton?20:30);
		Table table = useTable();
		
		table.defaults().pad(addButton?20:30);
		
		table.add(makeLabel(String.join("\n"+(addButton?"\n":""), text))).row();
		
		if(addButton)
			table.add(makeButton("Continue to World Config", () -> ClientCore.setScreen(new WorldGenScreen())));
		
		table.add(makeButton("Back", ClientCore::backToParentScreen)).row();
		
		// table.pack();
		// table.setBackground(new ColorRect(Color.TEAL));
	}
	
	@Override
	public void renderBackground() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	public static class InstructionsScreen extends InfoScreen {
		public InstructionsScreen() { this(false); }
		public InstructionsScreen(boolean forced) {
			super(forced, forced ? new String[] {
				"CONTROLS (more on instructions screen):",
				"Use mouse or arrow keys to move around.",
				"C to attack.",
				"V to interact with certain items or tiles.",
				"E to open your inventory (1-5 to set hotbar items).",
				"Z to open crafting screen (enter/click to craft items).",
				"Chat with T. Commands with /.",
				"",
				"INVENTORY NOTES:",
				"Inventory space is limited, the bar on the right shows fill amount.",
				"Hotbar slots don't count as extra inventory space."
			} : new String[] {
				"Use mouse or arrow keys to move around.",
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
				"Toggle Debug display: Shift-D"
			});
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
				"",
				/*"Music made by TrstN",*/
				"Sprites made by RiverOaken and Ross, with contributions by Theta and MadDest",
				"",
				"Sound effects made by Chris J",
				"",
				"",
				"Game Dev is hard."
			);
		}
	}
}
