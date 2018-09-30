package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.JButton;

import miniventure.game.client.ClientCore;

public class ErrorScreen extends MenuScreen implements BackgroundInheritor {
	
	private BackgroundProvider gdxBackground;
	
	public ErrorScreen(String error) {
		super(true, false);
		add(makeLabel(error));
		
		add(Box.createVerticalStrut(50));
		
		
		JButton retryBtn = makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld());
		add(retryBtn);
		
		add(Box.createVerticalStrut(10));
		
		JButton returnBtn = makeButton("Back to main menu", () -> ClientCore.setScreen(getParentScreen() != null ? getParentScreen() : new MainMenu()));
		add(returnBtn);
	}
	
	@Override
	public void setBackground(final BackgroundProvider gdxBackground) {
		this.gdxBackground = gdxBackground;
	}
	
	@Override
	public BackgroundProvider getGdxBackground() {
		return gdxBackground;
	}
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
	
	@Override
	public void glDraw() {
		if(gdxBackground != null)
			gdxBackground.glDraw();
		else
			super.glDraw();
	}
}
