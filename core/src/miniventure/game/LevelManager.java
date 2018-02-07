package miniventure.game;

import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.Gdx;

public class LevelManager {
	
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
	
	private int curLevel;
	private Player mainPlayer;
	private float gameTime;
	
	LevelManager() {
		
	}
	
	boolean worldLoaded() { return worldLoaded; }
	
	void render(GameScreen game, MenuScreen menu) {
		if(!worldLoaded || mainPlayer == null) return;
		
		Level level = Level.getLevel(curLevel);
		if(level == null) return;
		
		// render if no menu, or menu that has part of screen (update camera if update or multiplayer)
		// check input if no menu
		// update if no menu, or multiplayer
		
		if(menu == null)
			game.handleInput(mainPlayer);
		
		boolean update = menu == null; // later add "|| multiplayer";
		
		if(menu == null || !menu.usesWholeScreen()) {
			game.render(mainPlayer, TimeOfDay.getTimeOfDay(gameTime).getSkyColors(gameTime), level);
		}
		
		if(update) {
			game.update(mainPlayer, level);
			gameTime += Gdx.graphics.getDeltaTime();
		}
	}
	
	String getTimeOfDayString() {
		return TimeOfDay.getTimeOfDay(gameTime).getTimeString(gameTime);
	}
	
	public void createWorld() {
		worldLoaded = false;
		LoadingScreen loadingScreen = new LoadingScreen();
		GameCore.setScreen(loadingScreen);
		curLevel = 0;
		gameTime = 0;
		/// IDEA How about I have MenuScreen be an interface; or make another interface that MenuScreen implements. The idea is that I can have displays that don't use Scene2D (like the the loading screen, or level transitions if that's a thing), since they don't have options.
		new Thread(() -> {
			Level.resetLevels(loadingScreen);
			respawn();
			worldLoaded = true;
			Gdx.app.postRunnable(() -> GameCore.setScreen(null));
		}).start();
	}
	
	// load world method here, param worldname
	
	// save world method here, param worldname
	
	public void exitToMenu() { // returns to title screen
		// set menu to main menu, and dispose of level/world resources
		worldLoaded = false;
	}
	
	public void respawn() {
		if(mainPlayer != null) mainPlayer.remove();
		mainPlayer = new Player();
		
		Level level = Level.getLevel(curLevel);
		
		level.spawnMob(mainPlayer);
	}
}
