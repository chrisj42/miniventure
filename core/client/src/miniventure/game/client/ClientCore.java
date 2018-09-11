package miniventure.game.client;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.GameProtocol.Message;
import miniventure.game.chat.InfoMessage;
import miniventure.game.item.InventoryScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.ui4.Screen;
import miniventure.game.ui4.TestScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MonoVoidFunction;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientCore extends ApplicationAdapter {
	
	private static GameScreen gameScreen;
	private static ClientWorld clientWorld;
	
	private static Music song;
	private static final HashMap<String, Sound> soundEffects = new HashMap<>();
	
	public static final InputHandler input = new InputHandler();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	private static Screen uiScreen;
	
	private final ServerStarter serverStarter;
	
	public static final MonoVoidFunction<Throwable> exceptionNotifier = throwable -> {
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
	};
	
	/*public static final void wrapex(Action a) {
		try {
			a.act();
		} catch(Throwable t) {
			exceptionHandler.uncaughtException(Thread.currentThread(), t);
			throw t;
		}
	}*/
	
	public ClientCore(ServerStarter serverStarter) {
		this.serverStarter = serverStarter;
	}
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		LoadingScreen loader = new LoadingScreen();
		loader.pushMessage("Initializing...");
		setScreen(loader);
		//System.out.println("start delay");
		MyUtils.delay(0, () -> Gdx.app.postRunnable(() -> {
			//System.out.println("end delay");
			GameCore.initGdx();
			
			gameScreen = new GameScreen();
			clientWorld = new ClientWorld(serverStarter, gameScreen);
			
			setScreen(new MainMenu());
			setMenu(new TestScreen());
		}));
	}
	
	@Override
	public void dispose () {
		if(gameScreen != null)
			gameScreen.dispose();
		
		if(menuScreen != null)
			menuScreen.dispose();
		
		GameCore.dispose();
	}
	
	@Override
	public void render() {
		input.update();
		
		if (clientWorld != null && clientWorld.worldLoaded())
			clientWorld.update(GameCore.getDeltaTime()); // renders as well
		
		hasMenu = menuScreen != null;
		
		if (menuScreen != null)
			menuScreen.act();
		if (menuScreen != null)
			menuScreen.draw();
		
		if(uiScreen != null) {
			// uiScreen.update();
			uiScreen.render();
		}
	}
	
	public static void setMenu(@Nullable Screen screen) {
		uiScreen = screen;
	}
	public static void setScreen(@Nullable MenuScreen screen) {
		if(menuScreen instanceof InventoryScreen) {
			//System.out.println("sending held item request to server for "+clientWorld.getMainPlayer().getHands().getUsableItem());
			getClient().send(new InventoryUpdate(clientWorld.getMainPlayer()));
		}
		
		if(menuScreen instanceof MainMenu && screen instanceof ErrorScreen)
			return; // ignore it.
		
		if(screen == null && menuScreen != null && menuScreen != gameScreen.chatScreen)
			menuScreen.dispose();
		else if(screen != null && menuScreen != null && menuScreen != gameScreen.chatScreen)
			screen.setParent(menuScreen);
		
		System.out.println("setting screen to " + screen);
		
		menuScreen = screen;
		if(menuScreen != null) menuScreen.focus();
		if(gameScreen == null) {
			Gdx.input.setInputProcessor(menuScreen == null ? input : menuScreen);
		}
		else {
			Gdx.input.setInputProcessor(menuScreen == null ? new InputMultiplexer(gameScreen.getGuiStage(), input) : new InputMultiplexer(menuScreen, gameScreen.getGuiStage()));
		}
		input.reset();
	}
	public static void backToParentScreen() {
		if(menuScreen != null && menuScreen.getParent() != null) {
			MenuScreen screen = menuScreen.getParent();
			System.out.println("setting screen back to " + screen);
			menuScreen.dispose(false);
			menuScreen = screen;
			Gdx.input.setInputProcessor(menuScreen);
			input.reset();
		}
	}
	
	public static void playSound(String soundName) {
		if(!soundEffects.containsKey(soundName))
			soundEffects.put(soundName, Gdx.audio.newSound(Gdx.files.internal("audio/effects/"+soundName+".wav")));
		
		Sound s = soundEffects.get(soundName);
		//System.out.println("playing sound "+soundName+": "+s);
		if(s != null)
			s.play();
	}
	
	public static Music setMusicTrack(@NotNull FileHandle file) {
		stopMusic();
		song = Gdx.audio.newMusic(file);
		return song;
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
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null)
			gameScreen.resize(width, height);
		
		if(menuScreen != null)
			menuScreen.getViewport().update(width, height, true);
		if(uiScreen != null)
			uiScreen.resize(width, height);
	}
	
	
	public static boolean hasMenu() { return hasMenu; }
	
	@Nullable
	public static MenuScreen getScreen() { return menuScreen; }
	
	public static ClientWorld getWorld() { return clientWorld; }
	public static GameClient getClient() { return clientWorld.getClient(); }
}
