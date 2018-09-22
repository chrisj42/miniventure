package miniventure.game.client;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MonoVoidFunction;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
	
	private static JPanel uiPanel;
	private final JPanel hudPanel;
	private final ServerStarter serverStarter;
	
	
	
	public static final MonoVoidFunction<Throwable> exceptionNotifier = throwable -> {
		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);
		
		JTextArea errorDisplay = new JTextArea(string.toString());
		errorDisplay.setEditable(false);
		JScrollPane errorPane = new JScrollPane(errorDisplay);
		JOptionPane.showMessageDialog(null, errorPane, "An error has occurred", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
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
	
	public ClientCore(JPanel hudPanel, JPanel uiPanel, ServerStarter serverStarter) {
		this.hudPanel = hudPanel;
		ClientCore.uiPanel = uiPanel;
		this.serverStarter = serverStarter;
	}
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		LoadingScreen loader = new LoadingScreen();
		loader.pushMessage("Initializing...");
		setScreen(loader);
		// TODO once things work, see if removing the 0 delay and just posting the runnable works fine, or if it's still necessary.
		MyUtils.delay(0, () -> Gdx.app.postRunnable(() -> {
			GameCore.initGdx();
			
			gameScreen = new GameScreen(hudPanel);
			clientWorld = new ClientWorld(serverStarter, gameScreen);
			
			setScreen(new MainMenu());
		}));
	}
	
	@Override
	public void dispose () {
		if(gameScreen != null)
			gameScreen.dispose();
		
		// if(menuScreen != null)
		// 	menuScreen.dispose();
		
		GameCore.dispose();
	}
	
	@Override
	public void render() {
		input.update();
		
		if (clientWorld != null && clientWorld.worldLoaded())
			clientWorld.update(GameCore.getDeltaTime()); // renders as well
		
		hasMenu = menuScreen != null;
		
		if(menuScreen != null)
			// some menus use libGDX to render some or all of their graphics (most likely the background); this is their opportunity to do so.
			menuScreen.glDraw();
		
		// if (menuScreen != null)
		// 	menuScreen.act();
		// if (menuScreen != null)
		// 	menuScreen.draw();
	}
	
	public static void setScreen(@Nullable MenuScreen screen) {
		// if(screen == menuScreen)
		// 	return; // nothing is happening here.
		
		if(menuScreen instanceof InventoryScreen) {
			//System.out.println("sending held item request to server for "+clientWorld.getMainPlayer().getHands().getUsableItem());
			getClient().send(new InventoryUpdate(clientWorld.getMainPlayer()));
		}
		
		if(menuScreen instanceof MainMenu && screen instanceof ErrorScreen)
			return; // ignore it.
		
		// if(screen == null && menuScreen != null && menuScreen != gameScreen.chatScreen)
		// 	menuScreen.dispose();
		if(screen != null && menuScreen != null && menuScreen != gameScreen.chatScreen)
			screen.setParentScreen(menuScreen); // when are you going to go from a chat screen to another screen...?
		
		System.out.println("setting screen to " + screen);
		
		if(menuScreen == gameScreen.chatScreen)
			gameScreen.chatOverlay.setVisible(true);
		if(screen == gameScreen.chatScreen)
			gameScreen.chatOverlay.setVisible(false);
		
		if(menuScreen != null)
			uiPanel.remove(menuScreen);
		
		if(screen != null) {
			uiPanel.add(screen);
			screen.doLayoutBehavior(uiPanel);
		}
		
		menuScreen = screen;
		if(menuScreen != null) menuScreen.focus();
		/*if(gameScreen == null) {
			Gdx.input.setInputProcessor(oldMenuScreen == null ? input : oldMenuScreen);
		}
		else {
			Gdx.input.setInputProcessor(oldMenuScreen == null ? new InputMultiplexer(gameScreen.getGuiStage(), input) : new InputMultiplexer(oldMenuScreen, gameScreen.getGuiStage()));
		}*/
		input.reset(menuScreen != null);
	}
	public static void backToParentScreen() {
		if(menuScreen != null && menuScreen.getParentScreen() != null) {
			MenuScreen screen = menuScreen.getParentScreen();
			System.out.println("setting screen back to " + screen);
			
			uiPanel.remove(menuScreen);
			uiPanel.add(screen);
			screen.doLayoutBehavior(screen);
			screen.focus();
			
			menuScreen = screen;
			// Gdx.input.setInputProcessor(menuScreen);
			input.reset(false);
		}
		// else
		// 	input.reset(true);
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
		
		// MenuScreen menu = getScreen();
		// if(menu != null)
		// 	menu.getViewport().update(width, height, true);
	}
	
	
	public static boolean hasMenu() { return hasMenu; }
	
	@Nullable
	public static MenuScreen getScreen() { return menuScreen; }
	
	public static JPanel getUiPanel() { return uiPanel; }
	
	public static ClientWorld getWorld() { return clientWorld; }
	public static GameClient getClient() { return clientWorld.getClient(); }
	
}
