package miniventure.game.desktop;

import miniventure.game.GameCore;
import miniventure.server.ServerWorld;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		if(arg.length > 0 && arg[0].equalsIgnoreCase("--server")) {
			new ServerWorld(Integer.parseInt(arg[1]), Integer.parseInt(arg[2])).run();
		} else {
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Miniventure " + GameCore.VERSION;
			config.width = GameCore.DEFAULT_SCREEN_WIDTH;
			config.height = GameCore.DEFAULT_SCREEN_HEIGHT;
			new LwjglApplication(new GameCore(), config);
		}
	}
}
