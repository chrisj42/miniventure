package miniventure.game.client;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.Message;
import miniventure.game.chat.InfoMessage;
import miniventure.game.client.FontStyle.StyleData;
import miniventure.game.item.InventoryScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.screen.util.BackgroundInheritor;
import miniventure.game.screen.util.BackgroundProvider;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.GenericEnum;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTileType;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
	
	public static final int DEFAULT_SCREEN_WIDTH = 800;
	public static final int DEFAULT_SCREEN_HEIGHT = 450;
	private static GameScreen gameScreen;
	private static ClientWorld clientWorld;
	
	public static boolean viewedInstructions = false;
	
	// debug flags
	static boolean debugInfo = false;
	// static boolean debugChunk = false;
	static boolean debugTile = false;
	static boolean debugInteract = false;
	public static boolean debugBounds = false;
	
	private static Music song;
	private static final HashMap<String, Sound> soundEffects = new HashMap<>();
	
	public static final InputHandler input = new InputHandler();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	
	private static final Object screenLock = new Object();
	private static SpriteBatch batch;
	private static Skin skin;
	private static FreeTypeFontGenerator fontGenerator;
	private static GlyphLayout layout = new GlyphLayout();
	// private static HashMap<Integer, BitmapFont> fonts = new HashMap<>();
	
	private final ServerManager serverStarter;
	
	public static boolean PLAY_MUSIC = false;
	
	public static final ValueFunction<Throwable> exceptionNotifier = throwable -> {
		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);
		
		JTextArea errorDisplay = new JTextArea(string.toString());
		errorDisplay.setEditable(false);
		JScrollPane errorPane = new JScrollPane(errorDisplay);
		JOptionPane.showMessageDialog(null, errorPane, "An error has occurred", JOptionPane.ERROR_MESSAGE);
	};
	
	public static final UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
		exceptionNotifier.act(throwable);
		throwable.printStackTrace();
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
		
		LoadingScreen loader = new LoadingScreen();
		loader.pushMessage("Initializing...", true);
		setScreen(loader);
		//System.out.println("start delay");
		MyUtils.delay(0, () -> Gdx.app.postRunnable(() -> {
			//System.out.println("end delay");
			GameCore.initGdxTextures();
			if(batch == null)
				batch = new SpriteBatch();
			
			GenericEnum.init();
			ClientTileType.init();
			serverStarter.init();
			
			gameScreen = new GameScreen();
			clientWorld = new ClientWorld(serverStarter, gameScreen);
			
			setScreen(new MainMenu());
		}));
	}
	
	@Override
	public void dispose () {
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
		input.update();
		
		getBatch().setColor(Color.WHITE);
		
		if (clientWorld != null && clientWorld.worldLoaded())
			clientWorld.update(GameCore.getDeltaTime()); // renders as well
		
		synchronized (screenLock) {
			hasMenu = menuScreen != null;
			
			if(menuScreen != null)
				menuScreen.act();
			if(menuScreen != null)
				menuScreen.draw();
		}
	}
	
	public static void setScreen(@Nullable MenuScreen screen) {
		synchronized (screenLock) {
			if(screen == menuScreen) return;
			
			if(menuScreen instanceof InventoryScreen) {
				((InventoryScreen) menuScreen).close();
			}
			
			if(menuScreen != null && screen != null && screen == menuScreen.getParent()) {
				backToParentScreen();
				return;
			}
			
			if((menuScreen instanceof MainMenu || menuScreen instanceof ErrorScreen) && screen instanceof ErrorScreen)
				return; // ignore it.
			
			if(menuScreen instanceof BackgroundProvider && screen instanceof BackgroundInheritor)
				((BackgroundInheritor) screen).setBackground((BackgroundProvider) menuScreen);
			
			if(screen != null) {
				// error and loading (and chat) screens can have parents, but cannot be parents.
				screen.setParent(menuScreen);
				screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			} else if(menuScreen != null && (gameScreen == null || menuScreen != gameScreen.chatScreen))
				menuScreen.dispose();
			
			GameCore.debug("setting screen to " + screen);
			
			if(gameScreen != null) {
				if(screen instanceof MainMenu) {
					gameScreen.chatScreen.reset();
					gameScreen.chatOverlay.reset();
				}
			}
			
			input.resetDelay();
			menuScreen = screen;
			if(menuScreen != null) menuScreen.focus();
			if(gameScreen == null) {
				Gdx.input.setInputProcessor(menuScreen == null ? input : new InputMultiplexer(input, menuScreen));
			} else {
				Gdx.input.setInputProcessor(menuScreen == null ? new InputMultiplexer(gameScreen.getGuiStage(), input) : new InputMultiplexer(input.repressDelay(.1f), menuScreen, gameScreen.getGuiStage()));
			}
		}
	}
	public static void backToParentScreen() {
		synchronized (screenLock) {
			if(menuScreen != null && menuScreen.getParent() != null) {
				MenuScreen screen = menuScreen.getParent();
				GameCore.debug("setting screen back to " + screen);
				if(gameScreen == null || menuScreen != gameScreen.chatScreen)
					menuScreen.dispose(false);
				screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				menuScreen = screen;
				Gdx.input.setInputProcessor(menuScreen);
				input.reset();
			}
			else {
				if(clientWorld.worldLoaded())
					setScreen(null);
				else if(!(menuScreen instanceof MainMenu))
					setScreen(new MainMenu());
			}
		}
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
	
	static void addMessage(Message msg) {
		gameScreen.chatOverlay.addMessage(msg);
		gameScreen.chatScreen.addMessage(msg);
	}
	static void addMessage(InfoMessage msg) {
		gameScreen.chatOverlay.addMessage(msg);
		gameScreen.chatScreen.addMessage(msg);
	}
	static void clearMessages() {
		gameScreen.chatOverlay.reset();
		gameScreen.chatScreen.reset();
	}
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null) // null check in case this is called before app is fully initialized
			gameScreen.resize(width, height);
		
		MenuScreen menu = getScreen();
		if(menu != null)
			menu.resize(width, height);
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
