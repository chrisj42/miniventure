package miniventure.game.screen;

import java.awt.Color;

import miniventure.game.client.ClientCore;

public class ErrorScreen extends MenuScreen implements BackgroundInheritor {
	
	private BackgroundProvider gdxBackground;
	
	public ErrorScreen(String error) {
		super(true);
		
		addComponent(makeLabel(error));
		
		addComponent(50, makeButton("Reconnect", () -> ClientCore.getWorld().rejoinWorld()));
		
		addComponent(10, 
					 makeButton("Back to main menu", 
								() -> ClientCore.setScreen(getParentScreen() != null ? getParentScreen() : new MainMenu())
					 )
		);
		
		setBackground(new Color(32, 173, 43));
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
