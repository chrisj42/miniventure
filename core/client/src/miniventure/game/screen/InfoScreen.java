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
				"LMB to attack, RMB to interact and use items.",
				"Tab to open crafting screen (click to craft items).",
				"Chat with T. Commands with /."
			} : new String[] {
				"Use arrow keys or wasd to move around.",
				"Left click to attack.",
				"Right click to place tiles and use items such as food.",
				"Tab to open crafting window, click to craft.",
				"Some items create tiles directly instead of items; for these, left click to place the tile, and right click to stop placing them.",
				"",
				"Q to drop an item from your inventory/hotbar.",
				"Use Shift-Q to drop all items in the stack.",
				"Scroll to change item selection, or use number keys to select the corresponding item.",
				"Click and drag items to reorganize them.",
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
				"Sprites made by RiverOaken and Ross, with contributions by Cyrano and MadDest",
				"",
				"Sound effects made by Chris J",
				"",
				"5-star QA tester: Kat",
				"",
				"Thanks to everyone above, as well as Mojoraven, the libGDX discord, and others, for help in bouncing off ideas and general planning.",
				"",
				"Game Dev is hard."
			);
		}
	}
}
