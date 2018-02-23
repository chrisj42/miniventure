package miniventure.game.desktop;

import miniventure.game.GameCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Miniventure "+GameCore.VERSION;
		config.width = GameCore.DEFAULT_SCREEN_WIDTH;
		config.height = GameCore.DEFAULT_SCREEN_HEIGHT;
		new LwjglApplication(new GameCore(), config);
	}
}
