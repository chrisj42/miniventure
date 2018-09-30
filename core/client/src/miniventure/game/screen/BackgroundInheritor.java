package miniventure.game.screen;

public interface BackgroundInheritor {
	
	void setBackground(MenuScreen gdxBackground);
	MenuScreen getGdxBackground();
	
	default void inheritBackground(BackgroundInheritor other) {
		setBackground(other.getGdxBackground());
	}
}
