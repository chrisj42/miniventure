package miniventure.game.desktop;

import miniventure.game.screen.GameCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Miniventure";
		config.width = GameCore.SCREEN_WIDTH;
		config.height = GameCore.SCREEN_HEIGHT;
		new LwjglApplication(new GameCore(), config);
	}
}
