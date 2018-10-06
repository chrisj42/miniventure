package miniventure.game.screen;

import miniventure.game.client.ClientCore;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ErrorScreen extends BackgroundInheritor {
	
	public ErrorScreen(String error) { this(error, true); }
	public ErrorScreen(String error, boolean allowRejoin) {
		
		Table table = useTable();
		
		table.add(makeLabel(error)).row();
		
		if(allowRejoin)
			table.add(makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld())).spaceTop(50).row();
		
		table.add(
			makeButton("Back to main menu", () -> ClientCore.setScreen(getParent() != null ? getParent() : new MainMenu()))
		).spaceTop(10).row();
		
		// setBackground(new Color(32, 173, 43));
	}
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
}
