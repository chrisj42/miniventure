package miniventure.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.Player;
import miniventure.server.ServerWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientWorld {
	
	/*
		This is what contains the world. GameCore will check if the world is loaded here, and if not it won't render the game screen. Though... perhaps this class should hold a reference to the game screen instead...? Because, if you don't have a world, you don't need a game screen...
		
		The world will be created with this.
		It holds references to the current game level.
		You use this class to start processes on the whole world, like saving, loading, creating.
			And accessing the main player... and respawning. Also changing the player level?
		
		Perhaps this instance can be fetched from GameCore.
		
		GameScreen... game screen won't do much, just do the rendering. 
	 */
	
	private boolean worldLoaded = false;
	
	private GameClient client;
	
	private Player mainPlayer;
	private float gameTime;
	
	ClientWorld() {
		
	}
	
	boolean worldLoaded() { return worldLoaded; }
	
	void updateAndRender(GameScreen game, MenuScreen menu) {
		if(!worldLoaded || mainPlayer == null) return;
		
		Level level = mainPlayer.getLevel();
		if(level == null) {
			if(!(menu instanceof RespawnScreen))
				GameCore.setScreen(new RespawnScreen());
			return;
		}
		
		// render if no menu, or menu that has part of screen (update camera if update or multiplayer)
		// check input if no menu
		
		if(menu == null)
			game.handleInput(mainPlayer);
		
		if(menu == null || !menu.usesWholeScreen())
			game.render(mainPlayer, TimeOfDay.getTimeOfDay(gameTime).getSkyColors(gameTime), level);
	}
	
	String getTimeOfDayString() {
		return TimeOfDay.getTimeOfDay(gameTime).getTimeString(gameTime);
	}
	
	public void setPlayer(Player player) {
		Level level = mainPlayer == null ? Level.getLevel(0) : mainPlayer.getLevel();
		if(mainPlayer != null)
			mainPlayer.remove();
		mainPlayer = player;
		if(level != null)
			level.addEntity(player);
	}
	
	public void createWorld(int width, int height) throws IOException {
		worldLoaded = false;
		
		LoadingScreen loadingScreen = new LoadingScreen();
		GameCore.setScreen(loadingScreen);
		gameTime = 0;
		
		Level.clearLevels();
		new Thread(() -> {
			loadingScreen.pushMessage("Starting private server...");
			
			Array<String> args = new Array<>(String.class);
			
			String separator = System.getProperty("file.separator");
			String classpath = System.getProperty("java.class.path");
			args.add(System.getProperty("java.home") + separator + "bin" + separator + "java");
			args.add("-cp");
			args.add(classpath);
			// "sun.java.command" gets the main class.
			args.addAll(System.getProperty("sun.java.command").split(" "));
			args.add("--server");
			args.add(width+"");
			args.add(height+"");
			try {
				Process server = new ProcessBuilder(args.shrink())/*.inheritIO()*/.start();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
				boolean loaded = false;
				while(!loaded) {
					String input = reader.readLine();
					if(input != null) {
						System.out.println("logged input " + input);
						if(input.equals(ServerWorld.doneMsg))
							loaded = true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			loadingScreen.editMessage("connecting to private server...");
			client = new GameClient(loadingScreen, "localhost");
			
			Gdx.app.postRunnable(() -> GameCore.setScreen(null));
		}).start();
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	public void exitToMenu() { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		worldLoaded = false;
		mainPlayer = null;
		Level.clearLevels();
		GameCore.setScreen(new MainMenu());
	}
	
	public boolean isKeepAlive(@NotNull WorldObject obj) { return obj.equals(mainPlayer); }
	
	public Array<WorldObject> getKeepAlives(@NotNull Level level) {
		/*Array<WorldObject> keepAlives = new Array<>();
		for(WorldObject obj: this.keepAlives)
			if(obj.getLevel() == level)
				keepAlives.add(obj);
		
		return keepAlives;*/
		return new Array<>(new WorldObject[] {mainPlayer});
	}
}
