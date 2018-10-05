package miniventure.game.screen;

public class BackgroundInheritor extends BackgroundProvider {
	
	private BackgroundProvider gdxBackground;
	
	public BackgroundInheritor() {
		super(false);
		// the background provider will clear the background if needed
	}
	
	@Override
	public boolean usesWholeScreen() {
		return gdxBackground != null && gdxBackground.usesWholeScreen();
	}
	
	public void setBackground(final BackgroundProvider gdxBackground) {
		if(gdxBackground instanceof BackgroundInheritor)
			setBackground(((BackgroundInheritor)gdxBackground).gdxBackground);
		else if(gdxBackground != this) // don't want an infinite loop going
			this.gdxBackground = gdxBackground;
	}
	
	@Override
	public void renderBackground() {
		if(gdxBackground != null)
			gdxBackground.renderBackground();
	}
	
	@Override
	public void resizeBackground(int width, int height) {
		if(gdxBackground != null)
			gdxBackground.resize(width, height);
	}
}
