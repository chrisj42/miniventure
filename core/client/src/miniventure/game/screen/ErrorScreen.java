package miniventure.game.screen;

import miniventure.game.core.ClientCore;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ErrorScreen extends MenuScreen {
	
	private final String error;
	
	public ErrorScreen(String error) { this(error, true); }
	public ErrorScreen(String error, boolean allowRejoin) {
		super(new ScreenViewport());
		this.error = error;
		
		Table table = useTable();
		
		table.add(makeLabel(error)).row();
		
		VisTextButton rejoin = makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld());
		if(allowRejoin)
			table.add(rejoin).spaceTop(50).row();
		
		VisTextButton back = makeButton("Back to main menu", () -> ClientCore.addScreen(getParent() != null ? getParent() : new MainMenu()));
		table.add(back).spaceTop(10).row();
		
		mapButtons(table, rejoin, back);
		setKeyboardFocus(table);
	}
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
	
	@Override
	public String toString() {
		return "ErrorScreen("+error+')';
	}
}
