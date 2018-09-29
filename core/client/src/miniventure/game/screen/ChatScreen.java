package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import miniventure.game.util.RelPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatScreen extends MenuScreen {
	
	private static final float MESSAGE_LIFE_TIME = 15; // time from post to removal.
	private static final float MESSAGE_FADE_TIME = 3; // duration taken to go from full opaque to fully transparent.
	private static final Color BACKGROUND = new Color(128, 128, 128, 255*4/5);
	
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
		
		input = new JTextField("") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(ClientCore.getUiPanel().getSize().width*2/5, super.getPreferredSize().height);
			}
		};
		input.setFocusTraversalKeysEnabled(false);
		
		if(timeOutMessages)
			input.setVisible(false);
		
		input.addActionListener(e -> {
			String text = input.getText();
			if(text.length() == 0 || text.equals("/")) {
				ClientCore.setScreen(null);
				return;
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
			
			ClientCore.getClient().send(new Message(text, GameCore.DEFAULT_CHAT_COLOR));
			ClientCore.setScreen(null);
		});
		
		input.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() != '\t')
					tabbing = false;
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				if(keycode == KeyEvent.VK_ESCAPE) {
					ClientCore.setScreen(null);
				}
				else if(keycode == KeyEvent.VK_UP) {
					if(previousCommands.size() > prevCommandIdx+1) {
						if(prevCommandIdx < 0)
							curCommand = input.getText();
						prevCommandIdx++;
						input.setText(previousCommands.get(prevCommandIdx));
						input.setCaretPosition(input.getText().length());
					}
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
				}
				else if(keycode == KeyEvent.VK_TAB) {
					if(!input.getText().startsWith("/")) return;
					
					String text = tabbing ? manualInput : input.getText().substring(1);
					if(!tabbing) {
						manualInput = text;
						tabbing = true;
						tabIndex = -1;
					}
					ClientCore.getClient().send(new TabRequest(manualInput, tabIndex));
					tabIndex++;
				}
			}
			@Override public void keyReleased(KeyEvent e) {}
		});
		
		add(input);
		add(Box.createVerticalGlue());
	}
	
	
	public void focus(String initText) {
		input.setText(initText);
		input.setCaretPosition(initText.length());
		ClientCore.setScreen(this);
		focus();
	}
	
	@Override
	public void focus() {
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
	
	private void addMessage(String msg, int color) { addMessage(msg, color, false); }
	private void addMessage(String msg, int color, boolean connect) {
		TimerLabel label = new TimerLabel(
		  msg, 
		  new Color(color),
		  connect ? null : Box.createVerticalStrut(5)
		);
		add(label, 1);
		if(label.spacer != null) add(label.spacer, 1);
		labelQueue.addFirst(label);
		while(labelQueue.size() > 10) {
			TimerLabel c = labelQueue.removeLast();
			remove(c);
			if(c.spacer != null) remove(c.spacer);
		}
	}
	
	public void autocomplete(TabResponse response) {
		if(!tabbing) return; // abandoned request
		if(!manualInput.equals(response.manualText)) return; // response doesn't match the current state
		input.setText("/"+response.completion);
		input.setCaretPosition(input.getText().length());
	}
	
	
	private static final Font font = new Font("Arial", Font.BOLD, 13);
	private class TimerLabel extends JTextArea {
		private boolean started = false;
		private long startTime;
		private final Component spacer;
		
		TimerLabel(String label, @NotNull Color color, @Nullable Component spacer) {
			super(label);
			this.spacer = spacer;
			setOpaque(false);
			setEditable(false);
			setFocusable(false);
			setFont(font);
			setLineWrap(true);
			setWrapStyleWord(true);
			setBorder(null);
			
			setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 254));
			setBackground(BACKGROUND);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(input.getPreferredSize().width, super.getPreferredSize().height);
		}
		
		@Override
		public Dimension getMaximumSize() {
			return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
			// g.setColor(Color.BLACK);
			super.paintComponent(g);
			/*Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(0.2f));
			g2.setColor(new Color(255, 255, 255, getForeground().getAlpha()));
			FontMetrics fm = g2.getFontMetrics();
			g2.translate(*//*getWidth()/2-fm1.stringWidth(s[1])/2*//*0, fm.getAscent());
			System.out.println(getText()+" has "+getLineCount()+" lines.");
			LineMetrics lm = fm.getLineMetrics(getText(), g2);
			for(int i = 0; i < getLineCount(); i++) {
				try {
					int off = getLineStartOffset(i);
					int len = getLineEndOffset(i) - off;
					g2.draw(font.createGlyphVector(g2.getFontRenderContext(), getText(off, len)).getOutline());
				} catch(BadLocationException e) {
					e.printStackTrace();
				}
				g2.translate(*//*getWidth()/2-fm1.stringWidth(s[1])/2*//*0, fm.getHeight());
			}*/
			
			if(useTimer) {
				long now = System.nanoTime();
				if(!started) {
					started = true;
					startTime = now;
					MyUtils.delay((int)(MESSAGE_LIFE_TIME*1000), () -> {
						ChatScreen.this.remove(this);
						if(spacer != null) ChatScreen.this.remove(spacer);
						labelQueue.remove(this);
					});
					MyUtils.delay((int)((MESSAGE_LIFE_TIME - MESSAGE_FADE_TIME)*1000), this::repaint);
				}
				else {
					float timeLeft = MESSAGE_LIFE_TIME - (float)((now-startTime)/1E9d);
					if(timeLeft < MESSAGE_FADE_TIME) {
						float alpha = Math.max(0, timeLeft / MESSAGE_FADE_TIME);
						Color c = getForeground();
						setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * BACKGROUND.getAlpha())));
						c = getBackground();
						setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * BACKGROUND.getAlpha())));
					}
				}
			}
		}
		
		@Override
		public String toString() {
			return "TimerLabel:"+getText();
		}
	}
}
