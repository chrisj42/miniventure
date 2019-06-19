package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class InfoScreen extends BackgroundInheritor {
	
	private InfoScreen(String... text) { this(false, text); }
	private InfoScreen(boolean addButton, String... text) {
		super(new ScreenViewport());
		
		// VerticalGroup vGroup = useVGroup(addButton?20:30);
		Table table = useTable();
		
		table.defaults().pad(30);
		
		table.add(makeLabel(String.join("\n"+(addButton?"\n":""), text))).row();
		
		VisTextButton cont = null;
		if(addButton) {
			cont = makeButton("Continue to World Config", () -> ClientCore.setScreen(new WorldGenScreen()));
			table.add(cont);
		}
		
		VisTextButton back = makeButton("Back", ClientCore::backToParentScreen); 
		table.add(back).row();
		
		// table.pack();
		// table.setBackground(new ColorRect(Color.TEAL));
		mapButtons(table, cont, back);
		setKeyboardFocus(table);
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
				"Use arrow keys or wasd to move around.",
				"LMB/C to attack, RMB/V to interact and use items.",
				"Z to open crafting screen (enter/click to craft items).",
				"Chat with T. Commands with /."
			} : new String[] {
				"Use arrow keys or wasd to move around.",
				"Left click or press C to attack.",
				"Right click or press V to place tiles and use items such as food.",
				"Z to open crafting window, click/enter to craft.",
				"",
				"Q to drop an item from your inventory/hotbar.",
				"Use Shift-Q to drop all items in the stack.",
				"Scroll to change item selection, or use number keys to select the row of items.",
				"",
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
