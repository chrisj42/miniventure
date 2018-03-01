package miniventure.client;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

import miniventure.game.GameCore;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.VisUI;

public class ClientCore extends ApplicationAdapter {
	
	private static GameScreen gameScreen;
	private static ClientWorld clientWorld;
	
	@Override
	public void create () {
		VisUI.load(Gdx.files.internal("skins/visui/uiskin.json"));
		
		GameCore.initGdx();
		
		gameScreen = new GameScreen();
		clientWorld = new ClientWorld(gameScreen);
		
		GameCore.setScreen(new MainMenu(clientWorld));
	}
	
	@Override
	public void dispose () {
		if(gameScreen != null)
			gameScreen.dispose();
		
		GameCore.dispose();
	}
		
	@Override
	public void render() {
		try {
			GameCore.updateAndRender(clientWorld);
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
	
	@Override
	public void resize(int width, int height) {
		if(gameScreen != null)
			gameScreen.resize(width, height);
		
		MenuScreen menu = GameCore.getScreen();
		if(menu != null)
			menu.getViewport().update(width, height, true);
	}
	
	
	public static ClientWorld getWorld() { return clientWorld; }
}
