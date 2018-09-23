package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
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
			@Override
			protected void paintComponent(Graphics g) {
				if(ClientCore.getScreen() == ChatScreen.this)
					super.paintComponent(g);
			}
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(ClientCore.getUiPanel().getSize().width*2/5, super.getPreferredSize().height);
			}
		};
		
		if(timeOutMessages)
			ClientUtils.setupTransparentAWTContainer(input);
		
		// input.setAlignmentX(RIGHT_ALIGNMENT);
		
		input.addActionListener(e -> {
			String text = input.getText();
			if(text.length() == 0 || text.equals("/")) {
				ClientCore.setScreen(null);
				return;// true;
			}
			
			// not nothing
			
			if(prevCommandIdx != 0) // don't add the entry if we are just redoing the previous action
				previousCommands.push(text);
			prevCommandIdx = -1;
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
		if(useTimer) {
			// add(new JButton("Hello!"));
			// add(new JButton("He!"));
			// add(new JButton("Helffffo"));
			setVisible(false);
		}
		add(Box.createVerticalGlue());
		// repack();
		// setPreferredSize(new Dimension(400, 300));
	}
	/*
	@Override
	public void paint(Graphics g) {
		SwingUtilities.invokeLater(() -> ClientUtils.setupTransparentAWTContainer(this));
		super.paint(g);
	}*/
	
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
		TimerLabel label = new TimerLabel(msg, color==null?null:new Color(color));
		// label.setAlignmentX(RIGHT_ALIGNMENT);
		if(!connect)
			add(Box.createVerticalStrut(5));
		add(label, 1);
		labelQueue.addFirst(label);
		if(getComponentCount() > 10) {
			Component c = labelQueue.removeLast();
			if(useTimer)
				System.out.println("removing label " +c);
			remove(c);
		}
		// repack();
		revalidate();
		repaint();
		if(useTimer) {
			System.out.println("msg added to "+this+": "+msg);
			System.out.println("immediate component count: "+getComponentCount());
			SwingUtilities.invokeLater(() -> {
				System.out.println("for msg: "+msg);
				System.out.println("new component count: "+getComponentCount());
				System.out.println("new bounds: "+getBounds());
				System.out.println("msg bounds: "+label.getBounds());
				System.out.println("msg pref bounds: "+label.getPreferredSize());
			});
		}
		
		label.invalidate();
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
		// private final boolean connect;
		
		TimerLabel(String label, @Nullable Color color) {
			super("<html><p>"+label+"</p>");
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
			if(useTimer && false) {
				long now = System.nanoTime();
				if(!started) {
					started = true;
					startTime = now;
					MyUtils.delay((int)(MESSAGE_LIFE_TIME*1000), () -> {
						ChatScreen.this.remove(this);
						labelQueue.remove(this);
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
			
			ChatScreen.this.setSize(ChatScreen.this.getPreferredSize());
			
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
