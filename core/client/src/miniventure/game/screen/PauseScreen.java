package miniventure.game.screen;

import miniventure.game.core.ClientCore;
import miniventure.game.screen.InfoScreen.InstructionsScreen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class PauseScreen extends MenuScreen {
	
	// TODO remove the background inheritor thing from the screens, like in sponly
	// menus don't render backgrounds; the background is either the game screen, or the main menu background which is rendered in absence of a game screen
	
	public PauseScreen() {
		Table table = useTable();
		
		table.defaults().pad(10);
		
		VisTextButton resume = makeButton("Resume", ClientCore::removeScreen);
		table.add(resume).row();
		
		VisTextButton save = makeButton("Save World", () -> {
			ClientCore.getWorld().saveWorld();
			ClientCore.removeScreen();
		});
		if(ClientCore.getWorld().isLocalWorld())
			table.add(save).row();
		
		VisTextButton instruct = makeButton("Instructions/Controls", () -> ClientCore.addScreen(new InstructionsScreen()));
		table.add(instruct).row();
		
		VisTextButton exit = makeButton("Main Menu", () -> ClientCore.addScreen(new ConfirmScreen(ClientCore.getWorld().isLocalWorld() ? "Exit World? Your progress will be saved." : "Leave Server?", () -> ClientCore.getWorld().exitWorld())));
		table.add(exit).row();
		
		mapButtons(table, resume, resume);
		
		setKeyboardFocus(table);
	}
}
