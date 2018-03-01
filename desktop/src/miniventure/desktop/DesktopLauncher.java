package miniventure.desktop;

import miniventure.client.ClientCore;
import miniventure.game.GameCore;
import miniventure.server.ServerCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] args) {
		boolean server = false;
		for(String arg: args)
			if(arg.equalsIgnoreCase("--server"))
				server = true;
		
		if(server) {
			ServerCore.main(args);
		} else {
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Miniventure " + GameCore.VERSION;
			config.width = GameCore.DEFAULT_SCREEN_WIDTH;
			config.height = GameCore.DEFAULT_SCREEN_HEIGHT;
			new LwjglApplication(new ClientCore(), config);
		}
	}
}
