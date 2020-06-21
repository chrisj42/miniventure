package miniventure.game.core;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.VersionInfo;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.management.WorldDataSet;
import miniventure.game.world.management.WorldManager;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection StaticNonFinalField*/
public class GameCore {
	
	// DEBUG
	
	public static boolean debug = false;
	
	public static boolean debugBounds = false;
	// debug flags
	static boolean debugInfo = false;
	// static boolean debugChunk = false;
	// static boolean debugTile = false;
	static boolean debugInteract = false;
	
	public static final long PROGRAM_START = System.nanoTime();
	
	// GAME DATA DIRECTORY
	
	@NotNull public static final String DEFAULT_GAME_DIR;
	public static Path GAME_DIR = null;
	static {
		String home = System.getProperty("user.home");
		if(System.getProperty("os.name").contains("Windows")) {
			DEFAULT_GAME_DIR = home + "/Documents/My Games/Miniventure/";
			// WorldFileInterface.migrate(home + "/AppData/Roaming/Miniventure/");
		}
		else
			DEFAULT_GAME_DIR = home + "/.miniventure/";
	}
	
	/// --- APPLICATION ENTRY POINT ---
	
	public static void main(String[] args) throws IOException {
		Locale.setDefault(Locale.ENGLISH);
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
		
		if(args.length > 0 && args[0].equals("--debug"))
			debug = true;
		
		// follow the redirect if it exists and there wasn't a command line override
		if(GAME_DIR == null) {
			File redirect = new File(DEFAULT_GAME_DIR, Preferences.PREF_REDIRECT);
			if(redirect.exists())
				GAME_DIR = new File(String.join("", Files.readAllLines(redirect.toPath()))).toPath();
			else
				GAME_DIR = new File(DEFAULT_GAME_DIR).toPath();
		}
		
		WorldFileInterface.initGameDir();
		
		MyUtils.debug("Starting GUI client...");
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Miniventure - " + Version.CURRENT;
		config.width = GdxCore.DEFAULT_SCREEN_WIDTH;
		config.height = GdxCore.DEFAULT_SCREEN_HEIGHT;
		new LwjglApplication(new GdxCore(), config);
	}
	
	
	// WORLD MANAGEMENT
	
	private static WorldManager world;
	
	@Nullable
	public static WorldManager getWorld() { return world; }
	
	public static void startWorld(WorldDataSet worldInfo) {
		// perhaps notify if a world is already running
		// create a world manager with this world data and set it as the active world
		// remove all open menus
		// note: the world gen screen will follow this with setting the menu to instructions
		world = new WorldManager(worldInfo/*, new LoadingScreen()*/);
	}
	
	public static void exitWorld() {
		if(world == null) return;
		
		// save and then set to null, and open main menu
		world.saveWorld();
		world.dispose();
		world = null;
		GdxCore.setScreen(new MainMenu());
	}
	
	
	// VERSION CHECKING
	
	private static VersionInfo latestVersion = null;
	
	public static boolean determinedLatestVersion() { return latestVersion != null; }
	@NotNull
	public static VersionInfo getLatestVersion() {
		if(latestVersion != null)
			return latestVersion;
		
		// fetch the latest version from github
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repos/chrisj42/miniventure/releases").asJson();
			if(response.getStatus() != 200) {
				System.err.println("version request returned status code "+response.getStatus()+": "+response.getStatusText());
				System.err.println("response body: "+response.getBody());
				return latestVersion = new VersionInfo(Version.CURRENT, "", "");
			}
			else {
				return latestVersion = new VersionInfo(response.getBody().getArray().getJSONObject(0));
			}
		} catch(UnirestException e) {
			e.printStackTrace();
			return latestVersion = new VersionInfo(Version.CURRENT, "", "");
		}
	}
	
	public static final UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
		throwable.printStackTrace();
		
		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);
		
		JTextArea errorDisplay = new JTextArea(string.toString());
		errorDisplay.setEditable(false);
		JScrollPane errorPane = new JScrollPane(errorDisplay);
		JOptionPane.showMessageDialog(null, errorPane, "An error has occurred", JOptionPane.ERROR_MESSAGE);
		
		System.exit(1);
	};
	
	// public static GameClient getClient() { return clientWorld.getClient(); }
}
