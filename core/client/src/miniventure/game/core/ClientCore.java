package miniventure.game.core;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.chat.InfoMessage;
import miniventure.game.core.FontStyle.StyleData;
import miniventure.game.network.GameClient;
import miniventure.game.network.GameProtocol.DatalessRequest;
import miniventure.game.network.GameProtocol.Message;
import miniventure.game.network.ServerManager;
import miniventure.game.screen.ConfirmScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.NotifyScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.world.entity.ClientEntityRenderer;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTileType;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection StaticNonFinalField*/
public class ClientCore extends ApplicationAdapter {
	
	public static final int DEFAULT_SCREEN_WIDTH = 1400;
	public static final int DEFAULT_SCREEN_HEIGHT = 800;
	
	/// This variable is sort of a cache of the variable stored in the ClientWorld instance, so that ClientCore can perform operations with it. It's null more often than the ClientWorld's value because it gets reset when coming back to the main menu, but the ClientWorld's value doesn't. The reason we can't just do everything with the screen in ClientWorld is because that would require making more GameScreen methods/fields public; having some things in ClientCore means we can take advantage of package-private access.
	private static GameScreen gameScreen;
	
	/// This ClientWorld instance handles the world-related actions of a client-side game, and its persistent across multiple game instances; i.e. it's created once during initialization and then handles each new world in turn as they're loaded.
	private static ClientWorld clientWorld;
	
	// determines whether the instructions are shown before starting a new world
	public static boolean viewedInstructions = false;
	
	// debug flags
	static boolean debugInfo = false;
	// static boolean debugChunk = false;
	// static boolean debugTile = false;
	static boolean debugInteract = false;
	public static boolean debugBounds = false;
	
	private static Music song;
	private static final HashMap<String, Sound> soundEffects = new HashMap<>();
	
	/// The InputHandler class handles most game-related inputs and controls, i.e. stuff shown by the GameScreen instead of things in menus. But some menus do still check the InputHandler, such as for list scrolling with keys, so it's important to always multiplex this InputHandler after the respective MenuScreen Stage.
	public static final InputHandler input = new InputHandler();
	
	// This boolean is separate from the MenuScreen instance so that removal of the screen during MenuScreen act() is not reflected until the following frame.
	private static boolean hasMenu = false;
	// The current screen being shown. May have parent screens, which will not be rendered.
	private static MenuScreen menuScreen;
	// a lock object to prevent multithreading mixups while changing the screen.
	private static final Object screenLock = new Object();
	
	private static DisplayLevelBackground displayLevelBackground;
	
	// the batch used in most of the code
	private static SpriteBatch batch;
	// the ui skin used for most of the Scene2D elements
	private static Skin skin;
	// a font generator to allow for free font size scaling so the UI can still look half-decent regardless of window dimensions.
	private static FreeTypeFontGenerator fontGenerator;
	// lays out characters.
	private static GlyphLayout layout = new GlyphLayout();
	// private static HashMap<Integer, BitmapFont> fonts = new HashMap<>();
	
	/// manages all inter-operations with the server module of the code, used when starting single-player worlds. This allows the client and server module to be compiled independently while still allowing the various screens in this module to manage the state of a single-player server.
	private final ServerManager serverStarter;
	
	public static boolean PLAY_MUSIC = false;
	
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
	
	public ClientCore(ServerManager serverStarter) {
		this.serverStarter = serverStarter;
	}
	
	@Override
	public void create () {
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
		skin = new Skin(Gdx.files.internal("skins/visui/uiskin.json"));
		// Style.loadStyles(skin);
		VisUI.load(skin);
		
		LoadingScreen loader = new LoadingScreen(true);
		loader.pushMessage("Initializing");
		setScreen(loader);
		//System.out.println("start delay");
		// this delay is to allow the GDX backend to start up the render loop and render the loading screen we just set, before we start actually initializing all the things.
		MyUtils.delay(0, () -> Gdx.app.postRunnable(() -> {
			//System.out.println("end delay");
			GameCore.initGdxTextures();
			if(batch == null)
				batch = new SpriteBatch();
			
			// GenericEnum.init();
			ClientTileType.init();
			ClientEntityRenderer.init();
			serverStarter.init();
			
			// gameScreen = new GameScreen();
			clientWorld = new ClientWorld(serverStarter);
			
			displayLevelBackground = new DisplayLevelBackground();
			
			MenuScreen devNotice = new NotifyScreen(() -> setScreen(new MainMenu()),
				"Continue",
				"Welcome to Miniventure Alpha!",
				"",
				"This game is not finished!!! Not even close.",
				"Though the back-end may change from time to time, who knows.",
				"",
				"The utmost effort is made to prevent bugs from getting into releases, but since",
				"the sole developer has a life and very few testers, this effort only goes so far.",
				"",
				"Also, please keep in mind that, while the gameplay systems are (fairly) stable,",
				"the majority of content is yet to come.",
				
				"You may have to create new worlds to take advantage of some of the bigger updates,",
				"as the world is still being fleshed out.",
				"",
				"Enjoy the game!"
			).useWholeScreen();
			
			setScreen(devNotice);
			
			Path oldDataPath = WorldFileInterface.getDataImportSource();
			if(oldDataPath != null)
				addScreen(new ConfirmScreen("The default save location for miniventure files has changed since the previous version,\nso your files have been copied to the new location.\nDo you wish to delete the old save location? Older versions will lose their data.", () -> {
					WorldFileInterface.deleteRecursively(oldDataPath);
					removeScreen();
				}, ClientCore::removeScreen).useWholeScreen());
		}));
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		
		fontGenerator.dispose();
		for(HashMap<Integer, BitmapFont> sizedFonts: fonts.values())
			for(BitmapFont font: sizedFonts.values())
				font.dispose();
		fonts.clear();
		
		if(gameScreen != null)
			gameScreen.dispose();
		
		if(menuScreen != null)
			menuScreen.dispose();
		
		GameCore.dispose();
		VisUI.dispose();
	}
	
	@Override
	public void render() {
		getBatch().setColor(Color.WHITE);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		input.onFrameStart();
		
		MenuScreen curScreen = getScreen();
		final float delta = MyUtils.getDeltaTime();
		if(clientWorld != null) {
			if (clientWorld.worldLoaded())
				clientWorld.update(delta); // renders as well
			else if(curScreen == null || !curScreen.usesWholeScreen())
				displayLevelBackground.render(delta);
		}
		
		synchronized (screenLock) {
			hasMenu = menuScreen != null;
			
			if(menuScreen != null)
				menuScreen.act();
			if(menuScreen != null)
				menuScreen.draw();
		}
		
		input.onFrameEnd();
	}
	
	private static void resetInputProcessor() {
		if(gameScreen == null) {
			Gdx.input.setInputProcessor(menuScreen == null ? input : new InputMultiplexer(input, menuScreen));
		} else {
			Gdx.input.setInputProcessor(menuScreen == null ? new InputMultiplexer(gameScreen.getGuiStage(), input) : new InputMultiplexer(input.repressDelay(.1f), menuScreen, gameScreen.getGuiStage()));
		}
		
		if(GameCore.debug) {
			InputProcessor processor = Gdx.input.getInputProcessor();
			MyUtils.debug("input processors: " + (processor instanceof InputMultiplexer ? Arrays.toString(((InputMultiplexer) processor).getProcessors().items) : processor.toString()));
		}
	}
	
	public static void addScreen(@NotNull MenuScreen screen) { setScreen(screen, true); }
	private static void setScreen(@NotNull MenuScreen screen) { setScreen(screen, false); }
	private static void setScreen(@NotNull MenuScreen screen, boolean addToParent) {
		synchronized (screenLock) {
			if(screen == menuScreen) return;
			
			/*if(menuScreen != null && screen != null && screen == menuScreen.getParent()) {
				removeScreen();
				return;
			}*/
			
			if((menuScreen instanceof MainMenu || menuScreen instanceof ErrorScreen) && screen instanceof ErrorScreen)
				return; // ignore it.
			
			// determine if the given screen instance is already somewhere on the "screen" hierarchy, and if so go back to that one.
			MenuScreen check = menuScreen;
			while(check != null && check != screen)
				check = check.getParent();
			if(check != null) {
				// screen found
				while(menuScreen != null && menuScreen != screen)
					removeScreen();
				
				return;
			}
			
			
			// if(menuScreen instanceof BackgroundProvider && screen instanceof BackgroundInheritor)
			// 	((BackgroundInheritor) screen).setBackground((BackgroundProvider) menuScreen);
			
			if(addToParent)
				screen.setParent(menuScreen);
			else if(menuScreen != null && (gameScreen == null || menuScreen != gameScreen.chatScreen))
				menuScreen.dispose();
			
			MyUtils.debug("setting screen to " + screen);
			
			if(gameScreen != null && screen instanceof MainMenu) {
				gameScreen.chatScreen.reset();
				gameScreen.chatOverlay.reset();
				gameScreen = null; // the ClientWorld still saves the gameScreen in its instance; it will still be used for creating the new screen. This just means the chat screens don't get reset over and over again.
			}
			
			input.resetDelay();
			menuScreen = screen;
			resetInputProcessor();
			screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			if(menuScreen != null) menuScreen.focus();
		}
	}
	public static void removeScreen() { removeScreen(false); }
	public static void removeScreen(boolean all) {
		synchronized (screenLock) {
			if(menuScreen != null) {
				// screens are getting removed
				MenuScreen parent = menuScreen.getParent();
				if(gameScreen == null || menuScreen != gameScreen.chatScreen)
					menuScreen.dispose(all);
				menuScreen = null;
				if(!all && parent != null)
					setScreen(parent);
			}
			
			// add back the main menu if we don't have any other screens and no world
			if(menuScreen == null && !clientWorld.worldLoaded())
				setScreen(new MainMenu());
			
			if(menuScreen == null) {
				MyUtils.debug("setting screen to null");
				resetInputProcessor();
			}
		}
	}
	
	public static GameScreen newGameScreen(GameScreen screen) {
		gameScreen = screen;
		gameScreen.getGuiStage().focus();
		resetInputProcessor();
		return screen;
	}
	
	public static void playSound(String soundName) {
		if(!soundEffects.containsKey(soundName)) {
			try {
				soundEffects.put(soundName, Gdx.audio.newSound(Gdx.files.internal("audio/effects/" + soundName + ".wav")));
			} catch(GdxRuntimeException e) {
				System.err.println("error loading sound '"+soundName+"'; not playing.");
				return;
			}
		}
		
		Sound s = soundEffects.get(soundName);
		//System.out.println("playing sound "+soundName+": "+s);
		if(s != null)
			s.play();
	}
	
	public static Music setMusicTrack(@NotNull FileHandle file) throws AudioException {
		try {
			stopMusic();
			song = Gdx.audio.newMusic(file);
			return song;
		} catch(GdxRuntimeException e) {
			throw new AudioException(e);
		}
	}
	public static void stopMusic() {
		if(song != null) {
			song.stop();
			song.dispose();
		}
		song = null;
	}
	
	public static void manageChatPackets(Object obj) {
		if(gameScreen == null) return;
		if(obj instanceof Message) {
			gameScreen.chatOverlay.addMessage((Message)obj);
			gameScreen.chatScreen.addMessage((Message)obj);
		}
		else if(obj instanceof InfoMessage) {
			gameScreen.chatOverlay.addMessage((InfoMessage)obj);
			gameScreen.chatScreen.addMessage((InfoMessage)obj);
		}
		else if(obj == DatalessRequest.Clear_Console) {
			gameScreen.chatOverlay.reset();
			gameScreen.chatScreen.reset();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null) // null check in case this is called before app is fully initialized
			gameScreen.resize(width, height);
		
		MenuScreen menu = getScreen();
		if(menu != null)
			menu.resize(width, height);
		
		if(displayLevelBackground != null)
			displayLevelBackground.resize(width, height);
	}
	
	
	public static SpriteBatch getBatch() {
		if(batch == null) batch = new SpriteBatch();
		return batch;
	}
	
	public static GlyphLayout getTextLayout(String text) { return getTextLayout(getFont(), text); }
	public static GlyphLayout getTextLayout(BitmapFont font, String text) {
		if(fontGenerator != null)
			layout.setText(font, text);
		return layout;
	}
	
	private static EnumMap<FontStyle, HashMap<Integer, BitmapFont>> fonts = new EnumMap<>(FontStyle.class);
	
	public static BitmapFont getFont() { return getFont(FontStyle.Default); }
	public static BitmapFont getFont(FontStyle fontStyle) {
		// determine size difference in current window size vs default/minimum window size
		int minDefault = Math.min(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
		int minCurrent = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		// get the font parameter object named by the given style (throw error if none found)
		HashMap<Integer, BitmapFont> sizeFonts = fonts.computeIfAbsent(fontStyle, style -> new HashMap<>());
		
		StyleData style = fontStyle.get();
		FreeTypeFontParameter params = style.font;
		// apply font size modification
		// the process should maintain the ratios: orig font size / min default screen = new font size / min current screen
		// aka, new font size = orig font size * min current size / min default size
		if(style.resize) {
			params.size = params.size * minCurrent / minDefault;
			params.borderWidth = params.borderWidth * minCurrent / minDefault;
		}
		
		return sizeFonts.computeIfAbsent(params.size, fontSize -> fontGenerator.generateFont(params));
	}
	
	public static boolean hasMenu() { synchronized (screenLock) { return hasMenu; } }
	
	@Nullable
	public static MenuScreen getScreen() { synchronized (screenLock) { return menuScreen; } }
	
	public static ClientWorld getWorld() { return clientWorld; }
	public static GameClient getClient() { return clientWorld.getClient(); }
	
	@Override
	public void resume() {
		synchronized (screenLock) {
			if(menuScreen != null) {
				menuScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
		}
	}
}
