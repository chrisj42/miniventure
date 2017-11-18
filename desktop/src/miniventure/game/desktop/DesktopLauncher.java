package miniventure.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import miniventure.game.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Miniventure";
		config.width = Game.SCREEN_WIDTH;
		config.height = Game.SCREEN_HEIGHT;
		new LwjglApplication(new Game(), config);
	}
}
