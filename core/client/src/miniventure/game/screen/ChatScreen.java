package miniventure.game.screen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.Message;
import miniventure.game.GameProtocol.TabRequest;
import miniventure.game.GameProtocol.TabResponse;
import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.client.ClientCore;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;

public class ChatScreen extends MenuScreen {
	
	private static final float MESSAGE_LIFE_TIME = 15; // time from post to removal.
	private static final float MESSAGE_FADE_TIME = 3; // duration taken to go from full opaque to fully transparent.
	private static final Color BACKGROUND = Color.GRAY.cpy().sub(0, 0, 0, 0.25f);
	
	private static final int COMMAND_BUFFER_SIZE = 30;
	
	private final Deque<TimerLabel> labelQueue = new ArrayDeque<>();
	private final LinkedList<String> previousCommands = new LinkedList<>();
	private int prevCommandIdx = -1;
	private String curCommand;
	
	private final boolean useTimer;
	
	private final TextField input;
	
	private boolean tabbing = false;
	private int tabIndex = -1;
	private String manualInput = ""; // this is the part of the command that the user entered manually, and so should not be changed when tabbing.
	
	public ChatScreen(boolean timeOutMessages) {
		useTimer = timeOutMessages;
		
		vGroup.align(Align.topLeft);
		vGroup.columnAlign(Align.left);
		vGroup.space(5);
		vGroup.setWidth(getWidth()/2);
		
		input = new TextField("", GameCore.getSkin()) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(ClientCore.getScreen() == ChatScreen.this)
					super.draw(batch, parentAlpha);
			}
		};
		input.setWidth(getWidth()/2);
		input.setAlignment(Align.left);
		
		input.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					String text = input.getText();
					if(text.length() == 0) return true;
					if(text.equals("/")) return true;
					
					// valid command
					
					if(prevCommandIdx != 0) // don't add the entry if we are just redoing the previous action
						previousCommands.push(text);
					prevCommandIdx = -1;
					if(previousCommands.size() > COMMAND_BUFFER_SIZE)
						previousCommands.pollLast(); // remove oldest command from history
					
					if(!text.startsWith("/"))
						text = "msg " + text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\""); // replace one backslash with two.
					else
						text = text.substring(1);
					
					ClientCore.getClient().send(new Message(text, (String)null));
					ClientCore.setScreen(null);
					return true;
				}
				else if(keycode == Keys.ESCAPE) {
					ClientCore.setScreen(null);
					return true;
				}
				else if(keycode == Keys.UP) {
					if(previousCommands.size() > prevCommandIdx+1) {
						if(prevCommandIdx < 0)
							curCommand = input.getText();
						prevCommandIdx++;
						input.setText(previousCommands.get(prevCommandIdx));
						input.setCursorPosition(input.getText().length());
					}
					return true;
				}
				else if(keycode == Keys.DOWN) {
					if(prevCommandIdx+1 > 0) {
						prevCommandIdx--;
						if(prevCommandIdx == -1)
							input.setText(curCommand);
						else
							input.setText(previousCommands.get(prevCommandIdx));
					}
					input.setCursorPosition(input.getText().length());
					return true;
				}
				else if(keycode == Keys.TAB) {
					if(!input.getText().startsWith("/")) return true;
					
					String text = tabbing ? manualInput : input.getText().substring(1);
					if(!tabbing) {
						manualInput = text;
						tabbing = true;
						tabIndex = -1;
					}
					ClientCore.getClient().send(new TabRequest(manualInput, tabIndex));
					tabIndex++;
					
					return true;
				}
				else
					return false;
			}
			
			@Override
			public boolean keyTyped (InputEvent event, char character) {
				if(character != '\t')
					tabbing = false;
				// else
				// 	return true;
				return false;
			}
		});
		
		addActor(input);
		repack();
	}
	
	public void focus() { focus(""); }
	public void focus(String initText) {
		ClientCore.setScreen(this);
		input.setText(initText);
		input.pack();
		input.setWidth(getWidth()/2);
		input.setCursorPosition(initText.length());
		setKeyboardFocus(input);
	}
	
	public void addMessage(InfoMessage msg) {
		synchronized (labelQueue) {
			// add in reverse order
			for(int i = msg.lines.length - 1; i >= 0; i--) {
				InfoMessageLine line = msg.lines[i];
				addMessage(new Label(line.line, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(line.color))), i != 0);
			}
		}
	}
	public void addMessage(Message msg) {
		synchronized (labelQueue) {
			addMessage(new Label(msg.msg, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(msg.color))));
		}
	}
	
	private void addMessage(Label msg) { addMessage(msg, false); }
	private void addMessage(Label msg, boolean connect) {
		msg.setWrap(true);
		TimerLabel label = new TimerLabel(msg, connect);
		vGroup.addActorAt(0, label);
		labelQueue.add(label);
		if(vGroup.getChildren().size > 10)
			vGroup.removeActor(labelQueue.poll());
		repack();
	}
	
	public void autocomplete(TabResponse response) {
		if(!tabbing) return; // abandoned request
		if(!manualInput.equals(response.manualText)) return; // response doesn't match the current state
		input.setText("/"+response.completion);
		input.setCursorPosition(input.getText().length());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	private void repack() {
		input.pack();
		input.setWidth(getWidth()/2);
		input.setPosition(getWidth() / 2, getHeight() - input.getHeight());
		vGroup.pack();
		vGroup.setPosition(getWidth() / 2, input.getY() - 10 - vGroup.getHeight());
	}
	
	private class TimerLabel extends Container<Label> {
		float timeLeft;
		private final boolean connect;
		
		TimerLabel(Label label, boolean connect) {
			super(label);
			this.connect = connect;
			width(ChatScreen.this.getWidth()/2);
			timeLeft = MESSAGE_LIFE_TIME;
		}
		
		@Override
		public void act(float delta) {
			super.act(delta);
			
			if(!useTimer) return;
			timeLeft = Math.max(0, timeLeft-delta);
			if(timeLeft == 0) {
				vGroup.removeActor(this);
				repack();
				labelQueue.remove(this);
			}
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			float alpha = parentAlpha;
			
			// apply alpha, if fading.
			if(timeLeft < MESSAGE_FADE_TIME)
				alpha *= (timeLeft / MESSAGE_FADE_TIME);
			
			MyUtils.fillRect(getX(), getY(), ChatScreen.this.getWidth()/2, getHeight()+(connect?vGroup.getSpace():0), BACKGROUND.cpy().mul(1, 1, 1, alpha), batch);
			
			super.draw(batch, alpha);
		}
	}
}
