package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class PauseScreen extends BackgroundInheritor {
	
	public PauseScreen() {
		Table table = useTable();
		
		table.defaults().pad(10);
		
		VisTextButton resume = makeButton("Resume", () -> ClientCore.setScreen(null));
		table.add(resume).row();
		
		VisTextButton save = makeButton("Save World", () -> {
			ClientCore.getWorld().saveWorld();
			ClientCore.setScreen(null);
		});
		if(ClientCore.getWorld().isLocalWorld())
			table.add(save).row();
		
		VisTextButton instruct = makeButton("Instructions/Controls", () -> ClientCore.setScreen(new InstructionsScreen()));
		table.add(instruct).row();
		
		VisTextButton exit = makeButton("Main Menu", () -> ClientCore.setScreen(new ConfirmScreen(ClientCore.getWorld().isLocalWorld() ? "Exit World? Your progress will be saved." : "Leave Server?", () -> ClientCore.getWorld().exitWorld())));
		table.add(exit).row();
		
		mapButtons(table, resume, resume);
		
		setKeyboardFocus(table);
	}
	
	@Override
	public boolean allowChildren() {
		return true;
	}
}
