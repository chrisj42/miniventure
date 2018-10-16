package miniventure.game.desktop;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ServerManager;
import miniventure.game.server.ServerCore;
import miniventure.game.util.function.ValueFunction;

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
		for(String arg: args) {
			if(arg.equalsIgnoreCase("--server"))
				server = true;
			if(arg.equalsIgnoreCase("--debug"))
				GameCore.debug = true;
		}
		
		if(server) {
			ServerCore.main(args);
		} else {
			Thread.setDefaultUncaughtExceptionHandler(ClientCore.exceptionHandler);
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Miniventure " + GameCore.VERSION;
			config.width = GameCore.DEFAULT_SCREEN_WIDTH;
			config.height = GameCore.DEFAULT_SCREEN_HEIGHT;
			new LwjglApplication(new ClientCore(new ServerManager() {
				@Override
				public void startServer(int worldWidth, int worldHeight, ValueFunction<Boolean> callback) {
					boolean started = ServerCore.initServer(worldWidth, worldHeight, false);
					if(!started)
						callback.act(false);
					else {
						// server running, and world loaded; now, get the server world updating
						new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
						callback.act(true); // ready to connect
					}
				}
				
				@Override
				public void closeServer() { ServerCore.quit(); }
				
			}), config);
		}
	}
}
