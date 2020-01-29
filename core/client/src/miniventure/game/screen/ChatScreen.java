package miniventure.game.screen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import miniventure.game.core.GameCore;
import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.core.ClientCore;
import miniventure.game.core.FontStyle;
import miniventure.game.network.GameProtocol.Message;
import miniventure.game.network.GameProtocol.TabRequest;
import miniventure.game.network.GameProtocol.TabResponse;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;

import org.jetbrains.annotations.Nullable;

public class ChatScreen extends MenuScreen {
	
	private static final float MESSAGE_LIFE_TIME = 6; // time from post to removal.
	private static final float MESSAGE_FADE_TIME = 2; // duration taken to go from full opaque to fully transparent.
	private static final Color BACKGROUND = Color.GRAY.cpy().sub(0, 0, 0, 0.25f);
	
	private static final int COMMAND_BUFFER_SIZE = 30;
	private static final int MESSAGE_BUFFER_SIZE = 30;
	private static final int MESSAGE_DISPLAY_SIZE = 10;
	
	private final Deque<TimerLabel> labelQueue = new ArrayDeque<>();
	private final LinkedList<String> previousCommands = new LinkedList<>();
	private int prevCommandIdx = -1;
	private String curCommand;
	
	private final boolean useTimer;
	
	private final TextField input;
	private final VerticalGroup messageStream;
	
	@Nullable
	private final ScrollPane scrollPane;
	
	private boolean tabbing = false;
	private int tabIndex = -1;
	private String manualInput = ""; // this is the part of the command that the user entered manually, and so should not be changed when tabbing.
	
	public ChatScreen(boolean timeOutMessages) {
		super(false, new ScreenViewport());
		useTimer = timeOutMessages;
		
		VerticalGroup vGroup = useVGroup(0, Align.topRight, false);
		addMainGroup(vGroup, RelPos.TOP_RIGHT);
		vGroup.padTop(10).padRight(10);
		
		messageStream = new VerticalGroup() {
			@Override
			public float getPrefWidth() {
				if(scrollPane != null)
					return scrollPane.getScrollWidth();
				else
					return ChatScreen.this.getWidth() / 3;
			}
		};
		messageStream.align(Align.topRight);
		messageStream.columnAlign(Align.topRight);
		messageStream.space(5);
		
		input = new TextField("", VisUI.getSkin()) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(ClientCore.getScreen() == ChatScreen.this)
					super.draw(batch, parentAlpha);
			}
			@Override
			public float getPrefWidth() {
				return ChatScreen.this.getWidth()/3;
			}
		};
		registerField(input);
		
		vGroup.addActor(input);
		
		if(useTimer) {
			scrollPane = null;
			vGroup.addActor(messageStream);
		} else {
			scrollPane = new ScrollPane(messageStream, VisUI.getSkin()) {
				@Override
				public float getPrefWidth() {
					return ChatScreen.this.getWidth() / 3;
				}
				
				@Override
				public float getPrefHeight() {
					return Math.min(messageStream.getPrefHeight(), ChatScreen.this.getHeight()*2/3);
				}
			};
			scrollPane.setScrollingDisabled(true, false);
			scrollPane.setFadeScrollBars(false);
			vGroup.addActor(scrollPane);
			setScrollFocus(scrollPane);
		}
		
		input.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					String text = input.getText();
					if(text.length() == 0) return true;
					if(text.equals("/")) return true;
					
					// not nothing
					
					// don't add the entry if we are just redoing the previous action
					if(prevCommandIdx != 0 || !text.equals(previousCommands.get(prevCommandIdx)))
						previousCommands.push(text);
					prevCommandIdx = -1;
					if(previousCommands.size() > COMMAND_BUFFER_SIZE)
						previousCommands.pollLast(); // remove oldest command from history
					
					if(!text.startsWith("/"))
						text = "msg " + text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\""); // replace one backslash with two.
					else
						text = text.substring(1);
					
					ClientCore.getClient().send(new Message(text, GameCore.DEFAULT_CHAT_COLOR));
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
	}
	
	public void reset() {
		previousCommands.clear();
		deregisterLabels(labelQueue.toArray(new Label[0]));
		labelQueue.clear();
		messageStream.clearChildren();
	}
	
	public void focus(String initText) {
		input.setText(initText);
		input.setCursorPosition(initText.length());
		if(scrollPane != null)
			scrollPane.setScrollPercentY(0);
		ClientCore.setScreen(this);
	}
	@Override
	public void focus() {
		super.focus();
		setKeyboardFocus(input);
	}
	
	public void addMessage(InfoMessage msg) {
		synchronized (labelQueue) {
			// add in reverse order
			for(int i = msg.lines.length - 1; i >= 0; i--) {
				InfoMessageLine line = msg.lines[i];
				addMessage(line.line, line.color, i != 0);
			}
		}
	}
	public void addMessage(Message msg) {
		synchronized (labelQueue) {
			addMessage(msg.msg, msg.color);
		}
	}
	
	private void addMessage(String msg, String color) { addMessage(msg, color, false); }
	private void addMessage(String msg, String color, boolean connect) {
		Gdx.app.postRunnable(() -> {
			TimerLabel label = new TimerLabel(msg, Color.valueOf(color), connect);
			messageStream.addActorAt(0, label);
			labelQueue.add(label);
			registerLabel(FontStyle.Default, label);
			if(messageStream.getChildren().size > (useTimer ? MESSAGE_DISPLAY_SIZE : MESSAGE_BUFFER_SIZE)) {
				Label oldLabel = labelQueue.poll();
				deregisterLabels(oldLabel);
				messageStream.removeActor(oldLabel);
			}
		});
	}
	
	public void autocomplete(TabResponse response) {
		if(!tabbing) return; // abandoned request
		if(!manualInput.equals(response.manualText)) return; // response doesn't match the current state
		Gdx.app.postRunnable(() -> {
			input.setText('/'+response.completion);
			input.setCursorPosition(input.getText().length());
		});
	}
	
	private class TimerLabel extends Label {
		float timeLeft;
		private final boolean connect;
		
		TimerLabel(String text, Color color, boolean connect) {
			super(text, new LabelStyle(ClientCore.getFont(), color));
			this.connect = connect;
			timeLeft = MESSAGE_LIFE_TIME;
			setAlignment(Align.left);
			setWrap(true);
		}
		
		@Override
		public float getPrefWidth() {
			Group parent = getParent();
			if(parent != null)
				return parent.getWidth();
			else
				return ChatScreen.this.getWidth() / 3;
		}
		
		@Override
		public void act(float delta) {
			super.act(delta);
			
			if(!useTimer) return;
			timeLeft = Math.max(0, timeLeft-delta);
			if(timeLeft == 0) {
				Gdx.app.postRunnable(() -> {
					messageStream.removeActor(this);
					labelQueue.remove(this);
					deregisterLabels(this);
				});
			}
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			float alpha = parentAlpha;
			
			// apply alpha, if fading.
			if(timeLeft < MESSAGE_FADE_TIME)
				alpha *= (timeLeft / MESSAGE_FADE_TIME);
			
			MyUtils.fillRect(getX(), getY(), getWidth(), getHeight()+(connect?messageStream.getSpace():0), BACKGROUND, alpha, batch);
			
			try {
				super.draw(batch, alpha);
			} catch(Exception e) {
				System.err.println("error rendering TimerLabel text");
				e.printStackTrace();
			}
		}
	}
}
