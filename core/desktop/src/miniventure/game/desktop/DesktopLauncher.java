package miniventure.game.desktop;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.Preferences;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ServerManager;
import miniventure.game.server.ServerCore;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.file.WorldDataSet;
import miniventure.game.world.tile.ServerTileType;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	
	/**
	 * Main method for run the application
	 * 
	 * @param args arguments of application
	 */
	public static void main (String[] args) throws IOException {
		List<String> arglist = new LinkedList<>(Arrays.asList(args));
		
		if(arglist.contains("--help")) {
			System.out.println("Parameter options:");
			System.out.println("\t--help      display this help message.");
			System.out.println("\t--savedir   change where game data is stored and searched for. The default location on your computer is \""+GameCore.DEFAULT_GAME_DIR +"\". Without this option, the default location is checked for \""+Preferences.PREF_REDIRECT+"\", which tells the game where to look for the actual preferences and world saves. Using this option replaces the function of \""+Preferences.PREF_REDIRECT+"\", but it will not modify the file, so the option must be specified every time. To modify the file, use the options menu in the GUI client.");
			System.out.println("\t--server    run as a headless server (no graphical windows). See below..");
			System.out.println("Headless server usage format:");
			System.out.println("  --server \"world name\" [--port PORT] [--create [--seed \"seed\"] [--overwrite]]");
			System.out.println("Explanation:");
			System.out.println("\tWorld name is required. Square brackets \"[]\" denote optional arguments. As is implied by the nesting, the other options are ignored if --create is not specified.");
			System.out.println("\t--port: server is started on the given TCP port instead of the default port ("+GameProtocol.PORT+").");
			System.out.println("\tIt is first determined if the game dir contains a world save matching the given name.");
			System.out.println("\tWith no server options (only world name): if a match is found, it will be loaded, otherwise you will be prompted to create the world.");
			System.out.println("\tWith --create option: if no match is found, it will create the world without prompt, otherwise it will confirm you wish to overwrite the match with a new save.");
			System.out.println("\tWith --create --overwrite: a new world is created; if a match was found, it will overwrite it without prompt, otherwise it will create it as normal.");
			System.out.println("\tWith --create --seed \"seed\" (and optionally --overwrite): the world created will use the seed value given. See above for function of --create and --overwrite options.");
			return;
		}
		
		boolean server = false;
		ArrayList<String> leftover = new ArrayList<>(args.length);
		while(arglist.size() > 0) {
			final String arg = arglist.remove(0);
			switch(arg) {
				case "--debug": GameCore.debug = true; break;
				
				case "--savedir":
					GameCore.GAME_DIR = new File(arglist.remove(0)).getAbsoluteFile().toPath();
					break;
				
				case "--server": server = true; break;
				
				default:
					if(server) leftover.add(arg);
					else System.out.println("Unrecognized option: \""+arg+'"');
			}
		}
		
		// follow the redirect if it exists and there wasn't a command line override
		if(GameCore.GAME_DIR == null) {
			File redirect = new File(GameCore.DEFAULT_GAME_DIR, Preferences.PREF_REDIRECT);
			if(redirect.exists())
				GameCore.GAME_DIR = new File(String.join("", Files.readAllLines(redirect.toPath()))).toPath();
			else
				GameCore.GAME_DIR = new File(GameCore.DEFAULT_GAME_DIR).toPath();
		}
		
		if(server) {
			ServerCore.main(leftover.toArray(new String[0]));
		} else {
			GameCore.debug("Starting GUI client...");
			Thread.setDefaultUncaughtExceptionHandler(ClientCore.exceptionHandler);
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Miniventure - " + GameCore.VERSION;
			config.width = ClientCore.DEFAULT_SCREEN_WIDTH;
			config.height = ClientCore.DEFAULT_SCREEN_HEIGHT;
			// config.fullscreen = true;
			new LwjglApplication(new ClientCore(new ServerHolder()), config);
		}
	}
	
	private static class ServerHolder implements ServerManager {
		
		private ServerCore core;
		
		ServerHolder() {}
		
		@Override
		public void init() {
			ServerTileType.init();
		}
		
		@Override
		public boolean isHosting() {
			return core != null && core.isRunning();
		}
		
		@Override
		public int startServer(WorldDataSet worldInfo, MapFunction<InetSocketAddress, Boolean> hostFinder, ProgressLogger logger) throws IOException {
			this.core = ServerCore.initSinglePlayer(worldInfo, hostFinder, logger);
			int port = core.getServer().getPort();
			
			// server running, and world loaded; now, get the server world updating
			new Thread(new ThreadGroup("server"), core, "Miniventure Server").start();
			// ready to connect
			return port;
		}
		
		/*@Override
		public void setHost(InetSocketAddress host) {
			// System.out.println("setting host to "+host);
			core.getServer().setHost(host);
		}*/
		
		@Override
		public void open() { core.getServer().setMultiplayer(true); }
		
		@Override
		public void closeServer() {
			core.getWorld().exitWorld();
			core = null;
		}
		
	}
}
