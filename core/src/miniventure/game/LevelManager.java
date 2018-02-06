package miniventure.game;

import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

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
	
	private static final float LENGTH_OF_DAY = 60 * 5; // 5 minutes is like 24 hours in-game.
	private static final float DAWN_START_TIME = 5f/24;
	private static final float DAWN_END_TIME = 6f/24;
	private static final float DUSK_START_TIME = 20f/24;
	private static final float DUSK_END_TIME = 21f/24;
	
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
			Vector2 overlays = getDaylightOverlays();
			game.render(mainPlayer, overlays.x, overlays.y, level);
		}
		
		if(update) {
			game.update(mainPlayer, level);
			gameTime += Gdx.graphics.getDeltaTime();
		}
	}
	
	private Vector2 getDaylightOverlays() {
		if(gameTime < LENGTH_OF_DAY / 2) return new Vector2();
		
		/*
			5AM - 6AM = sunrise
			7PM - 8PM = sunset
		 */
		
		Vector2 alphas = new Vector2();
		
		float timeOfDay = (gameTime % LENGTH_OF_DAY) / LENGTH_OF_DAY;
		if(timeOfDay < DAWN_START_TIME || timeOfDay > DUSK_END_TIME)
			alphas.set(1, 0);
		else if(timeOfDay > DAWN_END_TIME && timeOfDay < DUSK_START_TIME)
			alphas.set(0, 0);
		else if(timeOfDay > DUSK_START_TIME) {
			alphas.x = (timeOfDay - DUSK_START_TIME) / (DUSK_END_TIME - DUSK_START_TIME);
			alphas.y = (alphas.x > 0.5f ? 1 - alphas.x : alphas.x) * 2;
		} else {
			alphas.x = 1 - ((timeOfDay - DAWN_START_TIME) / (DAWN_END_TIME - DAWN_START_TIME)); // alpha should start at 1
			alphas.y = (alphas.x > 0.5f ? 1 - alphas.x : alphas.x) * 2; // alpha should still start at zero 
		}
		
		// lightest at midday, darkest at midnight
		/*float timeSinceMidday = (gameTime + LENGTH_OF_HALF_DAY) % (LENGTH_OF_HALF_DAY*2);
		
		float alpha = timeSinceMidday / LENGTH_OF_HALF_DAY;
		if(alpha > 1)
			alpha = 2 - alpha;
		*/
		alphas.x *= 0.66f; // max of 0.66 alpha.
		alphas.y *= 0.40f;
		
		return alphas;
	}
	
	public String getTimeOfDayString() {
		float timeOfDay = (gameTime % LENGTH_OF_DAY) / LENGTH_OF_DAY;
		
		if(gameTime < DAWN_END_TIME * LENGTH_OF_DAY)
			return "First Morning Light, " + (Math.round((1-((DAWN_END_TIME - timeOfDay) / DAWN_END_TIME))*100))+"% past";
		
		
		float timeSinceDawn = (timeOfDay - DAWN_START_TIME + 1) % 1f;
		
		String timeString = "";
		
		String[] timeNames = {"", "Morning", "Day", "Dusk", "Night"};
		float[] times = {DAWN_START_TIME, DAWN_END_TIME, DUSK_START_TIME, DUSK_END_TIME, 1 + DAWN_START_TIME};
		for(int i = 0; i < times.length; i++)
			times[i] -= DAWN_START_TIME;
		
		for(int i = 1; i < times.length; i++) {
			if(timeSinceDawn < times[i]) {
				timeString += timeNames[i] + ", ";
				// now add percentage
				float duration = times[i] - times[i-1];
				float percent = (timeSinceDawn - times[i-1]) / duration;
				timeString += Math.round(percent * 100)+"% past";
				break;
			}
		}
		
		timeString += " ("+Math.round(timeOfDay*100)+"% through day)";
		
		return timeString;
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
