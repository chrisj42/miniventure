package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ErrorScreen extends BackgroundInheritor {
	
	private final String error;
	
	public ErrorScreen(String error) { this(error, true); }
	public ErrorScreen(String error, boolean allowRejoin) {
		super(new ScreenViewport());
		this.error = error;
		
		Table table = useTable();
		
		table.add(makeLabel(error)).row();
		
		if(allowRejoin)
			table.add(makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld())).spaceTop(50).row();
		
		table.add(
			makeButton("Back to main menu", () -> ClientCore.setScreen(getParent() != null ? getParent() : new MainMenu()))
		).spaceTop(10).row();
		
	}
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
	
	@Override
	public String toString() {
		return "ErrorScreen("+error+')';
	}
}
