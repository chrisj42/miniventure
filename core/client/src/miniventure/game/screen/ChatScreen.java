package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import miniventure.game.GameProtocol.Message;
import miniventure.game.GameProtocol.TabRequest;
import miniventure.game.GameProtocol.TabResponse;
import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.client.ClientCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;

import org.jetbrains.annotations.Nullable;

public class ChatScreen extends MenuScreen {
	
	private static final float MESSAGE_LIFE_TIME = 15; // time from post to removal.
	private static final float MESSAGE_FADE_TIME = 3; // duration taken to go from full opaque to fully transparent.
	private static final Color BACKGROUND = new Color(128, 128, 128, 255*3/5);
	
	private static final int COMMAND_BUFFER_SIZE = 30;
	
	private final Deque<TimerLabel> labelQueue = new ArrayDeque<>();
	private final LinkedList<String> previousCommands = new LinkedList<>();
	private int prevCommandIdx = -1;
	private String curCommand;
	
	private final boolean useTimer;
	
	private final JTextField input;
	
	private boolean tabbing = false;
	private int tabIndex = -1;
	private String manualInput = ""; // this is the part of the command that the user entered manually, and so should not be changed when tabbing.
	
	public ChatScreen(boolean timeOutMessages) {
		super(true, false);
		useTimer = timeOutMessages;
		
		// add(vGroup);
		//
		// vGroup.align(Align.topLeft);
		// vGroup.columnAlign(Align.left);
		// add(Box.createVerticalStrut(5));
		// vGroup.setWidth(getWidth()/2);
		
		input = new JTextField("") {
			/*@Override
			protected void paintComponent(Graphics g) {
				if(ClientCore.getScreen() == ChatScreen.this)
					super.paintComponent(g);
			}*/
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(ClientCore.getUiPanel().getSize().width*2/5, super.getPreferredSize().height);
			}
		};
		
		if(timeOutMessages)
			ClientUtils.setupTransparentAWTContainer(input);
		
		// setBackground(Color.BLUE);
		// setOpaque(true);
		
		// input.setAlignmentX(RIGHT_ALIGNMENT);
		
		input.addActionListener(e -> {
			String text = input.getText();
			if(text.length() == 0 || text.equals("/")) {
				ClientCore.setScreen(null);
				return;// true;
			}
			
			// not nothing
			
			
			// don't add the entry if we are just redoing the previous action
			if(prevCommandIdx != 0 || !text.equals(previousCommands.get(prevCommandIdx)))
				previousCommands.push(text);
			prevCommandIdx = -1; // reset the scrollback index
			if(previousCommands.size() > COMMAND_BUFFER_SIZE)
				previousCommands.pollLast(); // remove oldest command from history
			
			if(!text.startsWith("/"))
				text = "msg " + text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\""); // replace one backslash with two.
			else
				text = text.substring(1);
			
			ClientCore.getClient().send(new Message(text, (Integer)null));
			ClientCore.setScreen(null);
		});
		
		input.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() != '\t')
					tabbing = false;
				// return false;
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				if(keycode == KeyEvent.VK_ESCAPE) {
					ClientCore.setScreen(null);
					// return true;
				}
				else if(keycode == KeyEvent.VK_UP) {
					if(previousCommands.size() > prevCommandIdx+1) {
						if(prevCommandIdx < 0)
							curCommand = input.getText();
						prevCommandIdx++;
						input.setText(previousCommands.get(prevCommandIdx));
						input.setCaretPosition(input.getText().length());
					}
					// return true;
				}
				else if(keycode == KeyEvent.VK_DOWN) {
					if(prevCommandIdx+1 > 0) {
						prevCommandIdx--;
						if(prevCommandIdx == -1)
							input.setText(curCommand);
						else
							input.setText(previousCommands.get(prevCommandIdx));
					}
					input.setCaretPosition(input.getText().length());
					// return true;
				}
				else if(keycode == KeyEvent.VK_TAB) {
					if(!input.getText().startsWith("/")) return;// true;
					
					String text = tabbing ? manualInput : input.getText().substring(1);
					if(!tabbing) {
						manualInput = text;
						tabbing = true;
						tabIndex = -1;
					}
					ClientCore.getClient().send(new TabRequest(manualInput, tabIndex));
					tabIndex++;
					
					// return true;
				}
				// else
				// 	return false;
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
		});
		// add(input);
		add(input);
		if(useTimer)
			setVisible(false);
		add(Box.createVerticalGlue());
		// repack();
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_L) {
					System.out.println("focusing canvas");
					
				}
			}
		});
	}
	
	
	public void focus(String initText) {
		input.setText(initText);
		input.setCaretPosition(initText.length());
		ClientCore.setScreen(this);
	}
	
	@Override
	public void focus() {
		// input.pack();
		// input.setWidth(getWidth()/2);
		input.requestFocus();
		super.focus();
	}
	
	@Override
	public void doLayoutBehavior(Container parent) {
		ClientUtils.addToAnchorLayout(this, parent, RelPos.TOP_RIGHT, -5, 5);
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
	
	private void addMessage(String msg, Integer color) { addMessage(msg, color, false); }
	private void addMessage(String msg, Integer color, boolean connect) {
		// msg.setWrap(true);
		TimerLabel label = new TimerLabel(msg, color==null?null:new Color(color), connect ? null : Box.createVerticalStrut(5));
		// label.setAlignmentX(RIGHT_ALIGNMENT);
		add(label, 1);
		if(label.spacer != null) add(label.spacer, 1);
		labelQueue.addFirst(label);
		if(useTimer) {
			System.out.println("component count: " + getComponentCount());
			System.out.println("label queue size: " + labelQueue.size());
		}
		while(labelQueue.size() > 10) {
			TimerLabel c = labelQueue.removeLast();
			remove(c);
			if(c.spacer != null) remove(c.spacer);
			if(useTimer)
				System.out.println("component count after removal of "+c+": "+getComponentCount());
		}
		// repack();
		// revalidate();
		// repaint();
	}
	
	public void autocomplete(TabResponse response) {
		if(!tabbing) return; // abandoned request
		if(!manualInput.equals(response.manualText)) return; // response doesn't match the current state
		input.setText("/"+response.completion);
		input.setCaretPosition(input.getText().length());
	}
	
	// @Override
	// public boolean usesWholeScreen() { return false; }
	
	/*private void repack() {
		input.pack();
		input.setWidth(getWidth()/2);
		input.setPosition(getWidth() / 2, getHeight() - input.getHeight());
		try {
			vGroup.pack();
		} catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		vGroup.setPosition(getWidth() / 2, input.getY() - 10 - vGroup.getHeight());
	}*/
	
	private class TimerLabel extends JLabel {
		private boolean started = false;
		private long startTime;
		private final Component spacer;
		// private final boolean connect;
		
		TimerLabel(String label, @Nullable Color color, @Nullable Component spacer) {
			super("<html><p>"+label+"</p>");
			this.spacer = spacer;
			// this.connect = connect;
			// width(ChatScreen.this.getWidth()/2);
			// timeLeft = MESSAGE_LIFE_TIME;
			if(color != null)
				setForeground(color);
			setBackground(BACKGROUND);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(input.getPreferredSize().width, super.getPreferredSize().height);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			if(useTimer) {
				long now = System.nanoTime();
				if(!started) {
					started = true;
					startTime = now;
					MyUtils.delay((int)(MESSAGE_LIFE_TIME*1000), () -> {
						ChatScreen.this.remove(this);
						if(spacer != null) ChatScreen.this.remove(spacer);
						labelQueue.remove(this);
						// System.out.println("component count after removal of "+this+": "+ChatScreen.this.getComponentCount());
					});
				}
				else {
					float timeLeft = (float)((now-startTime)/1E9d);
					if(timeLeft < MESSAGE_FADE_TIME) {
						float alpha = timeLeft / MESSAGE_FADE_TIME;
						Color c = getForeground();
						setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
						c = getBackground();
						setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
					}
				}
			}
			
			super.paintComponent(g);
		}
		/*
		@Override
		public void draw(Batch batch, float parentAlpha) {
			float alpha = parentAlpha;
			
			// apply alpha, if fading.
			if(timeLeft < MESSAGE_FADE_TIME)
				alpha *= (timeLeft / MESSAGE_FADE_TIME);
			
			MyUtils.fillRect(getX(), getY(), ChatScreen.this.getWidth()/2, getHeight()+(connect?vGroup.getSpace():0), BACKGROUND, alpha, batch);
			
			super.draw(batch, alpha);
		}*/
		
		@Override
		public String toString() {
			return "TimerLabel:"+getText();
		}
	}
}
