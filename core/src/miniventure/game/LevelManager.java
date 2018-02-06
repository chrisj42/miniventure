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
	
	private static final float LENGTH_OF_HALF_DAY = .5f * 60 * 5; // 5 minutes is like 24 hours in-game.
	// private static final float DAWN_START_TIME = 5f/24;
	// private static final float DAWN_END_TIME = 6f/24;
	// private static final float DUSK_START_TIME = 20f/24;
	// private static final float DUSK_END_TIME = 21f/24;
	
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
		
		if(menu == null || !menu.usesWholeScreen())
			game.render(mainPlayer, getDaylightOverlay(), level);
		
		if(update) {
			game.update(mainPlayer, level);
			gameTime += Gdx.graphics.getDeltaTime();
		}
	}
	
	private float getDaylightOverlay() {
		if(gameTime < LENGTH_OF_HALF_DAY) return 0;
		
		/*
			5AM - 6AM = sunrise
			7PM - 8PM = sunset
		 */
		
		/*float alpha;
		
		float timeOfDay = gameTime % (LENGTH_OF_HALF_DAY * 2);
		if(timeOfDay < DAWN_START_TIME || timeOfDay > DUSK_END_TIME)
			alpha = 1;
		else if(timeOfDay > DAWN_END_TIME && timeOfDay < DUSK_START_TIME)
			alpha = 0;
		else if(timeOfDay > DUSK_START_TIME)
			alpha = (timeOfDay - DUSK_START_TIME) / (DUSK_END_TIME - DUSK_START_TIME);
		else
			alpha = (timeOfDay - DAWN_START_TIME) / (DAWN_END_TIME - DAWN_START_TIME);
		
		*/
		// lightest at midday, darkest at midnight
		float timeSinceMidday = (gameTime + LENGTH_OF_HALF_DAY) % (LENGTH_OF_HALF_DAY*2);
		
		float alpha = timeSinceMidday / LENGTH_OF_HALF_DAY;
		if(alpha > 1)
			alpha = 2 - alpha;
		
		alpha *= 0.66f; // max of 0.75 alpha.
		
		return alpha;
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
		mainPlayer = new Player();
		
		Level level = Level.getLevel(curLevel);
		
		level.spawnMob(mainPlayer);
	}
}
