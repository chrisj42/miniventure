package miniventure.game.client;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.HeldItemRequest;
import miniventure.game.item.InventoryScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class ClientCore extends ApplicationAdapter {
	
	private static GameScreen gameScreen;
	private static ClientWorld clientWorld;
	
	public static final InputHandler input = new InputHandler();
	
	private static boolean hasMenu = false;
	private static MenuScreen menuScreen;
	
	private final ServerStarter serverStarter;
	
	public static final UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
		StringWriter string = new StringWriter();
		PrintWriter printer = new PrintWriter(string);
		throwable.printStackTrace(printer);
		
		JTextArea errorDisplay = new JTextArea(string.toString());
		errorDisplay.setEditable(false);
		JOptionPane.showMessageDialog(null, errorDisplay, "An error has occurred", JOptionPane.ERROR_MESSAGE);
	};
	
	public ClientCore(ServerStarter serverStarter) {
		this.serverStarter = serverStarter;
	}
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		GameCore.initGdx();
		
		gameScreen = new GameScreen();
		clientWorld = new ClientWorld(serverStarter, gameScreen);
		
		setScreen(new MainMenu());
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
		try {
			input.update();
			
			if (clientWorld.worldLoaded())
				clientWorld.update(Gdx.graphics.getDeltaTime()); // renders as well
			
			hasMenu = menuScreen != null;
			if (menuScreen != null) {
				menuScreen.act();
				menuScreen.draw();
			}
		} catch(Throwable t) {
			//System.out.println("running threads: " + Thread.activeCount());
			
			exceptionHandler.uncaughtException(Thread.currentThread(), t);
			
			throw t;
		}
	}
	
	public static void setScreen(@Nullable MenuScreen screen) {
		if(menuScreen instanceof InventoryScreen) {
			System.out.println("sending held item request to server for "+clientWorld.getMainPlayer().getHands().getUsableItem());
			getClient().send(new HeldItemRequest(clientWorld.getMainPlayer().getHands()));
		}
		
		if(screen == null && menuScreen != null)
			menuScreen.dispose();
		else if(screen != null)
			screen.setParent(menuScreen);
		
		System.out.println("setting screen to " + screen);
		
		menuScreen = screen;
		Gdx.input.setInputProcessor(menuScreen == null ? input : menuScreen);
		input.reset();
	}
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null)
			gameScreen.resize(width, height);
		
		MenuScreen menu = getScreen();
		if(menu != null)
			menu.getViewport().update(width, height, true);
	}
	
	
	public static boolean hasMenu() { return hasMenu; }
	
	@Nullable
	public static MenuScreen getScreen() { return menuScreen; }
	
	public static ClientWorld getWorld() { return clientWorld; }
	public static GameClient getClient() { return clientWorld.getClient(); }
}
