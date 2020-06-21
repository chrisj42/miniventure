package miniventure.game.screen;

import miniventure.game.core.AudioCore;
import miniventure.game.core.GdxCore;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ErrorScreen extends MenuScreen {
	
	private final String error;
	
	public ErrorScreen(String error) {
		super(new ScreenViewport());
		this.error = error;
		
		Table table = useTable();
		
		table.add(makeLabel(error)).row();
		
		// VisTextButton rejoin = makeButton("Reconnect", () -> GameCore.getWorld().rejoinWorld());
		// if(allowRejoin)
		// 	table.add(rejoin).spaceTop(50).row();
		
		VisTextButton back = makeButton("Main Menu", () -> GdxCore.setScreen(getParent() != null ? getParent() : new MainMenu()));
		table.add(back).spaceTop(10).row();
		
		mapButtons(table, back, back);
		setKeyboardFocus(table);
	}
	
	@Override public void focus() { AudioCore.stopMusic(); super.focus(); }
	
	@Override
	public String toString() {
		return "ErrorScreen("+error+')';
	}
}
