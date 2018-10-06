package miniventure.game.desktop;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ServerManager;
import miniventure.game.server.ServerCore;
import miniventure.game.util.function.MonoVoidFunction;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

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
			Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
			config.setTitle("Miniventure " + GameCore.VERSION);
			config.setWindowedMode(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT);
			config.setWindowSizeLimits(GameCore.DEFAULT_SCREEN_WIDTH, GameCore.DEFAULT_SCREEN_HEIGHT, -1, -1);
			new Lwjgl3Application(new ClientCore(new ServerManager() {
				@Override
				public void startServer(int worldWidth, int worldHeight, MonoVoidFunction<Boolean> callback) {
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
