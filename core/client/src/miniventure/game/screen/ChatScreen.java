package miniventure.game.screen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.Message;
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
	
	private static final float MESSAGE_LIFE_TIME = 8; // time from post to removal.
	private static final float MESSAGE_FADE_TIME = 3; // duration taken to go from full opaque to fully transparent.
	private static final Color BACKGROUND = Color.GRAY.cpy().sub(0, 0, 0, 0.25f);
	
	private static final int COMMAND_BUFFER_SIZE = 30;
	
	private final Deque<TimerLabel> labelQueue = new ArrayDeque<>();
	private final LinkedList<String> previousCommands = new LinkedList<>();
	private int prevCommandIdx = -1;
	private String curCommand;
	
	private final boolean useTimer;
	
	private final TextField input;
	
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
		setKeyboardFocus(input);
		
		input.addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					String text = input.getText();
					if(text.length() == 0) return true;
					if(text.equals("/")) return true;
					if(!text.startsWith("/"))
						text = "msg " + text;
					else
						text = text.substring(1);
					
					ClientCore.getClient().send(new Message(text, (String)null));
					previousCommands.push(text);
					prevCommandIdx = -1;
					if(previousCommands.size() > COMMAND_BUFFER_SIZE)
						previousCommands.pollLast(); // remove oldest command from history
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
					return true;
				}
				else
					return false;//input.getDefaultInputListener().keyDown(event, keycode); // entered a normal key
			}
		});
		
		addActor(input);
		input.pack();
		input.setPosition(getWidth()/2, getHeight()-input.getHeight());
	}
	
	@Override
	protected void drawTable(Batch batch, float parentAlpha) {
		if(labelQueue.size() > 0)
			MyUtils.fillRect(vGroup.getX(), vGroup.getY(), getWidth()/2, vGroup.getPrefHeight()+10, BACKGROUND, batch);
	}
	
	public void focus() { focus(""); }
	public void focus(String initText) {
		ClientCore.setScreen(this);
		input.setText(initText);
		input.pack();
		input.setWidth(getWidth()/2);
		setKeyboardFocus(input);
	}
	
	public void addMessage(InfoMessage msg) {
		synchronized (labelQueue) {
			// add in reverse order
			for(int i = msg.lines.length - 1; i >= 0; i--) {
				InfoMessageLine line = msg.lines[i];
				addMessage(new Label(line.line, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(line.color))));
			}
		}
	}
	public void addMessage(Message msg) {
		synchronized (labelQueue) {
			addMessage(new Label(msg.msg, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(msg.color))));
		}
	}
	
	private void addMessage(Label msg) {
		msg.setWrap(true);
		Container<Label> container = new Container<>(msg).width(getWidth()/2);
		vGroup.addActorAt(0, container);
		labelQueue.add(new TimerLabel(container));
		if(vGroup.getChildren().size > 10)
			vGroup.removeActor(labelQueue.poll().label);
		vGroup.pack();
		input.pack();
		input.setPosition(getWidth()/2, getHeight()-input.getHeight());
		vGroup.setPosition(getWidth()/2, input.getY()-10-vGroup.getHeight());
	}
	
	@Override
	public boolean usesWholeScreen() { return false; }
	
	
	@Override
	public void act(float delta) {
		for(TimerLabel label: labelQueue.toArray(new TimerLabel[labelQueue.size()]))
			label.update(delta);
	}
	
	private class TimerLabel {
		final Container<Label> label;
		float timeLeft;
		
		TimerLabel(Container<Label> label) {
			this.label = label;
			timeLeft = MESSAGE_LIFE_TIME;
		}
		
		void update(float delta) {
			if(!useTimer) return;
			timeLeft = Math.max(0, timeLeft-delta);
			if(timeLeft == 0) {
				vGroup.removeActor(label);
				vGroup.pack();
				vGroup.setPosition(getWidth()/2, getHeight()-vGroup.getHeight());
				labelQueue.remove(this);
			}
		}
	}
}
