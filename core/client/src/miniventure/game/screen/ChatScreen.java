package miniventure.game.screen;

import java.util.ArrayDeque;
import java.util.Deque;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.Message;
import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.client.ClientCore;
import miniventure.game.util.MyUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

public class ChatScreen extends MenuScreen {
	
	private static final float MESSAGE_FADE_TIME = 8;
	private static final Color BACKGROUND = Color.GRAY.cpy().sub(0, 0, 0, 0.25f);
	
	private Deque<TimerLabel> labelQueue = new ArrayDeque<>();
	
	private final boolean useTimer;
	
	public ChatScreen(boolean timeOutMessages) {
		useTimer = timeOutMessages;
		
		vGroup.align(Align.topLeft);
		vGroup.columnAlign(Align.left);
		vGroup.space(5);
		vGroup.setWidth(getWidth()/2);
	}
	
	@Override
	protected void drawTable(Batch batch, float parentAlpha) {
		if(labelQueue.size() > 0)
			MyUtils.fillRect(vGroup.getX(), vGroup.getY(), getWidth()/2, vGroup.getPrefHeight()+10, BACKGROUND, batch);
	}
	
	public void sendMessage() {
		Gdx.input.getTextInput(new TextInputListener() {
			@Override
			public void input(String text) {
				ClientCore.getClient().send(new Message("msg "+text, (String)null));
				ClientCore.setScreen(null);
			}
			
			@Override
			public void canceled() {
				ClientCore.setScreen(null);
			}
		}, "Enter your message", "", "");
	}
	
	public void sendCommand() {
		Gdx.input.getTextInput(new TextInputListener() {
			@Override
			public void input(String text) {
				ClientCore.getClient().send(new Message(text, (String)null));
				ClientCore.setScreen(null);
			}
			
			@Override
			public void canceled() {
				ClientCore.setScreen(null);
			}
		}, "Enter command", "", "");
	}
	
	public void addMessage(InfoMessage msg) {
		for(InfoMessageLine line: msg.lines) {
			addMessage(new Label(line.line, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(line.color))));
		}
	}
	public void addMessage(Message msg) {
		addMessage(new Label(msg.msg, new Label.LabelStyle(GameCore.getFont(), Color.valueOf(msg.color))));
	}
	
	private void addMessage(Label msg) {
		msg.setWrap(true);
		Container<Label> container = new Container<>(msg).width(getWidth()/2);
		vGroup.addActorAt(0, container);
		labelQueue.add(new TimerLabel(container));
		if(vGroup.getChildren().size > 10)
			vGroup.removeActor(labelQueue.poll().label);
		vGroup.pack();
		vGroup.setPosition(getWidth()/2, getHeight()-vGroup.getHeight());
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
			timeLeft = MESSAGE_FADE_TIME;
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
