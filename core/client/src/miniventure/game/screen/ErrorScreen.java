package miniventure.game.screen;

import miniventure.game.client.ClientCore;

public class ErrorScreen extends BackgroundInheritor {
	
	public ErrorScreen(String error) { this(error, true); }
	public ErrorScreen(String error, boolean allowRejoin) {
		
		addComponent(makeLabel(error));
		
		if(allowRejoin)
			addComponent(50, makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld()));
		
		addComponent(10, 
			makeButton("Back to main menu", 
				() -> ClientCore.setScreen(getParent() != null ? getParent() : new MainMenu())
			)
		);
		
		// setBackground(new Color(32, 173, 43));
	}
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
}
