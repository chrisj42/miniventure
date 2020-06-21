package miniventure.game.screen;

import miniventure.game.core.GameCore;
import miniventure.game.core.GdxCore;
import miniventure.game.screen.InfoScreen.InstructionsScreen;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.jetbrains.annotations.NotNull;

public class PauseScreen extends MenuScreen {
	
	public PauseScreen(@NotNull WorldManager world) {
		Table table = useTable();
		
		table.defaults().pad(10);
		
		VisTextButton resume = makeButton("Resume", () -> GdxCore.setScreen(null));
		table.add(resume).row();
		
		VisTextButton save = makeButton("Save World", () -> {
			world.saveWorld();
			GdxCore.setScreen(null);
		});
		// if(GameCore.getWorld().isLocalWorld())
		table.add(save).row();
		
		VisTextButton instruct = makeButton("Instructions/Controls", () -> GdxCore.addScreen(new InstructionsScreen()));
		table.add(instruct).row();
		
		VisTextButton exit = makeButton("Main Menu", () -> GdxCore.addScreen(new ConfirmScreen("Exit World? Your progress will be saved.", GameCore::exitWorld)));
		table.add(exit).row();
		
		mapButtons(table, resume, resume);
		
		setKeyboardFocus(table);
	}
	
	/*@Override
	public boolean allowChildren() {
		return true;
	}*/
}
