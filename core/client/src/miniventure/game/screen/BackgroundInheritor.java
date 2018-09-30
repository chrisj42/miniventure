package miniventure.game.screen;

public interface BackgroundInheritor extends BackgroundProvider {
	
	void setBackground(BackgroundProvider gdxBackground);
	BackgroundProvider getGdxBackground();
	
	default void inheritBackground(BackgroundInheritor other) {
		setBackground(other.getGdxBackground());
	}
}
