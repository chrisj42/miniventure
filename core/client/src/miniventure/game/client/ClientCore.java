package miniventure.game.client;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

import miniventure.game.GameCore;
import miniventure.game.world.WorldManager;
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
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		GameCore.initGdx();
		
		gameScreen = new GameScreen();
		clientWorld = new ClientWorld(gameScreen);
		
		setScreen(new MainMenu(clientWorld));
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
			updateAndRender(clientWorld);
		} catch(Throwable t) {
			StringWriter string = new StringWriter();
			PrintWriter printer = new PrintWriter(string);
			t.printStackTrace(printer);
			
			JTextArea errorDisplay = new JTextArea(string.toString());
			errorDisplay.setEditable(false);
			JOptionPane.showMessageDialog(null, errorDisplay, "An error has occurred", JOptionPane.ERROR_MESSAGE);
			
			throw t;
		}
	}
	
	public static void updateAndRender(WorldManager world) {
		input.update();
		
		if (world.worldLoaded())
			world.update(Gdx.graphics.getDeltaTime());
		
		hasMenu = menuScreen != null;
		if (menuScreen != null) {
			menuScreen.act();
			menuScreen.draw();
		}
	}
	
	public static void setScreen(@Nullable MenuScreen screen) {
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
}
