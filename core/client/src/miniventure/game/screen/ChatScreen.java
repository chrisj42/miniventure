package miniventure.game.screen;

import java.util.ArrayDeque;
import java.util.Deque;

import miniventure.game.GameProtocol.Message;
import miniventure.game.client.ClientCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;

public class ChatScreen extends MenuScreen {
	
	private Deque<Cell<Label>> queue = new ArrayDeque<>();
	
	//private String curText = "";
	
	public ChatScreen() {
		
		vGroup.remove();
		
		addActor(table);
		
		table.setPosition(getWidth()/2, getHeight(), Align.topRight);
		table.setSkin(VisUI.getSkin());
		
		//Gdx.input.setInputProcessor(new InputMultiplexer(this, ClientCore.input));
		
		/*getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE)
					ClientCore.setScreen(null);
				else if(keycode == Keys.ENTER) {
					if(curText.length() > 0) {
						ClientCore.getClient().send(new Message(curText));
						curText = "";
					}
					ClientCore.setScreen(null);
				}
				else if(keycode == Keys.BACKSPACE && curText.length() > 0)
					curText = curText.substring(0, curText.length()-1);
				
				return true;
			}
		});*/
	}
	
	public void getInput() {
		Gdx.input.getTextInput(new TextInputListener() {
			@Override
			public void input(String text) {
				ClientCore.getClient().send(new Message(text));
				ClientCore.setScreen(null);
			}
			
			@Override
			public void canceled() {
				ClientCore.setScreen(null);
			}
		}, "Enter your message", "", "");
	}
	
	public void addMessage(String msg) {
		queue.add(table.add(msg));
		table.row();
		if(table.getRows() > 10)
			table.removeActor(queue.poll().getActor());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
}
