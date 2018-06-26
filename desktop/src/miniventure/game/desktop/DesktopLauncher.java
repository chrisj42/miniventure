package miniventure.game.desktop;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.server.ServerCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	
	/**
	 * Main method for run the application
	 * 
	 * @param args arguments of application
	 */
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
			new LwjglApplication(new ClientCore((width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
		}
	}
}
