package miniventure.game.desktop;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ServerManager;
import miniventure.game.screen.AnchorPanel;
import miniventure.game.server.ServerCore;
import miniventure.game.util.function.MonoVoidFunction;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;

public class DesktopLauncher {
	
	/**
	 * Main method for run the application
	 * 
	 * @param args arguments of application
	 */
	public static void main (String[] args) {
		boolean server = false;
		for(String arg: args) {
			if(arg.equalsIgnoreCase("--server"))
				server = true;
			if(arg.equalsIgnoreCase("--debug"))
				GameCore.debug = true;
		}
		
		if(server) {
			ServerCore.main(args);
		} else {
			Thread.setDefaultUncaughtExceptionHandler(ClientCore.exceptionHandler);
			LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
			config.title = "Miniventure " + GameCore.VERSION;
			config.width = GameCore.DEFAULT_SCREEN_WIDTH;
			config.height = GameCore.DEFAULT_SCREEN_HEIGHT;
			config.resizable = false;
			
			final Dimension WINDOW_SIZE = new Dimension(config.width, config.height);
			
			// the window frame
			JFrame frame = new JFrame(config.title);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // EXIT_ON_CLOSE interferes with libGDX shutdown
			frame.getContentPane().setPreferredSize(WINDOW_SIZE);
			
			JWindow uiFrame = makeUIFrame(frame);
			
			// the panel where swing UI components will be added (i.e. menu screens):
			AnchorPanel hudPanel = new AnchorPanel(uiFrame.getContentPane());
			
			// the panel where swing HUD components will be added (i.e. health/hunger bars, debug display, chat overlay):
			AnchorPanel uiPanel = new AnchorPanel(uiFrame.getContentPane());
			
			LwjglCanvas canvas = new LwjglCanvas(new ClientCore(hudPanel,
				uiPanel,
				
				() -> frame.dispatchEvent(new WindowEvent(frame,
				WindowEvent.WINDOW_CLOSING)),
				
				new ServerManager() {
					@Override
					public void startServer(final int worldWidth, final int worldHeight, final MonoVoidFunction<Boolean> callback) {
						boolean started = ServerCore.initServer(worldWidth, worldHeight, false);
						if(!started)
							callback.act(false);
						else {
							// server running, and world loaded; now, get the server world updating
							new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
							callback.act(true); // ready to connect
						}
					}
					
					@Override
					public void closeServer() {
						ServerCore.quit();
					}
				}), config);
			DesktopLauncher.canvas = canvas;
			
			// the canvas where libGDX rendering occurs
			Canvas awtCanvas = canvas.getCanvas();
			frame.add(awtCanvas);
			awtCanvas.setFocusable(false);
			awtCanvas.addFocusListener(new FocusListener() {
				@Override public void focusGained(final FocusEvent e) { uiFrame.requestFocus(); }
				@Override public void focusLost(final FocusEvent e) {}
			});
			
			addInputRedirects(uiFrame);
			frame.setFocusTraversalKeysEnabled(false);
			awtCanvas.setFocusTraversalKeysEnabled(false);
			
			// end the program when the window is closed; this way allows libGDX to shutdown correctly (I think...)
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("stopping gdx");
					canvas.exit();
					System.exit(0);
				}
			});
			
			// run the program in the event thread
			SwingUtilities.invokeLater(() -> {
				uiFrame.pack();
				frame.pack();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				Dimension frameSize = frame.getSize();
				frame.setLocation((screenSize.width - frameSize.width)/2, (screenSize.height - frameSize.height)/2);
				frame.setVisible(true);
				// Timer t = new Timer(3000, e -> System.out.println(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()));
				// t.setRepeats(true);
				// t.start();
			});
		}
	}
	
	private static LwjglCanvas canvas;
	
	private static JWindow makeUIFrame(JFrame frame) {
		JWindow uiFrame = new JWindow(frame) {
			@Override
			public void doLayout() {
				setSize(getPreferredSize());
				super.doLayout();
			}
			
			@Override
			public Dimension getPreferredSize() {
				return frame.getContentPane().getSize();
			}
		};
		
		uiFrame.setBackground(new Color(0, 0, 0, 0));
		uiFrame.setFocusable(true);
		
		uiFrame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(final WindowEvent e) { ClientCore.input.reset(true); }
			@Override
			public void windowLostFocus(final WindowEvent e) { ClientCore.input.reset(false); }
		});
		
		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) { uiFrame.revalidate(); }
			@Override
			public void componentMoved(ComponentEvent e) { uiFrame.setLocation(frame.getContentPane().getLocationOnScreen()); }
			@Override
			public void componentShown(ComponentEvent e) { uiFrame.setVisible(true); }
			@Override
			public void componentHidden(ComponentEvent e) { uiFrame.setVisible(false); }
		});
		
		return uiFrame;
	}
	
	private static void addInputRedirects(Component comp) {
		comp.setFocusTraversalKeysEnabled(false);
		
		// System.out.println("adding key listener to "+comp);
		comp.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// System.out.println("key typed on "+e.getSource());
				ClientCore.input.keyTyped(e.getKeyChar());
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// System.out.println("key pressed on "+e.getSource());
				ClientCore.input.keyDown(e.getExtendedKeyCode());
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// System.out.println("key released on "+e.getSource());
				ClientCore.input.keyUp(e.getExtendedKeyCode());
			}
		});
		
		/*comp.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				System.out.println("clicked "+e.getSource());
			}
			
			@Override
			public void mousePressed(final MouseEvent e) {
				System.out.println("pressed mouse "+e.getSource());
			}
			
			@Override
			public void mouseReleased(final MouseEvent e) {
				System.out.println("released mouse "+e.getSource());
			}
			
			@Override
			public void mouseEntered(final MouseEvent e) {
				System.out.println("entered "+e.getSource());
			}
			
			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("exited "+e.getSource());
			}
		});*/
	}
}
