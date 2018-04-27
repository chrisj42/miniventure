package miniventure.game.screen;

import miniventure.game.client.ClientCore;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class InstructionsScreen extends MenuScreen {
	
	public InstructionsScreen() {
		
		Array<String> lines = new Array<>(new String[] {
			"Use mouse or arrow keys to move around.",
			"C to attack, V to interact.",
			"E to open your inventory, Z to craft items.",
			"+ and - keys to zoom in and out.",
			"Press \"t\" to chat with other players, and \"/\" to use commands.",
			"(Hint: use the up key to repeat messages, and tab to autocomplete command names.)"
		});
		//"(press b to show/hide chunk boundaries)",
		VisLabel instructions = new VisLabel(String.join(System.lineSeparator(), lines.items));
		instructions.setWrap(true);
		instructions.setPosition(getWidth()/2, getHeight() - instructions.getPrefHeight() - 10, Align.center);
		addActor(instructions);
		
		VisTextButton returnBtn = makeButton("Back to Main Menu", ClientCore::backToParentScreen);
		returnBtn.setPosition(getWidth()/2, returnBtn.getPrefHeight()*3/2, Align.center);
		addActor(returnBtn);
	}
	
}
