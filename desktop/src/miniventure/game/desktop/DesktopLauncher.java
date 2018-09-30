package miniventure.game.desktop;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.*;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.AnchorPanel;
import miniventure.game.server.ServerCore;

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
			
			JFrame uiFrame = makeUIFrame(frame);
			
			// the panel where swing UI components will be added (i.e. menu screens):
			AnchorPanel hudPanel = new AnchorPanel(uiFrame);
			
			// the panel where swing HUD components will be added (i.e. health/hunger bars, debug display, chat overlay):
			AnchorPanel uiPanel = new AnchorPanel(uiFrame);
			
			LwjglCanvas canvas = new LwjglCanvas(new ClientCore(hudPanel, uiPanel, (width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
			
			
			// the canvas where libGDX rendering occurs
			Canvas awtCanvas = canvas.getCanvas();
			frame.add(awtCanvas);
			awtCanvas.setFocusable(true);
			awtCanvas.requestFocus();
			
			uiFrame.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
					e.setSource(awtCanvas);
					// System.out.println("forwarding key typed event: "+e);
					awtCanvas.dispatchEvent(e);
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					e.setSource(awtCanvas);
					// System.out.println("forwarding key pressed event: "+e);
					awtCanvas.dispatchEvent(e);
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					e.setSource(awtCanvas);
					// System.out.println("forwarding key released event: "+e);
					awtCanvas.dispatchEvent(e);
				}
			});
			
			uiFrame.setFocusTraversalKeysEnabled(false);
			frame.setFocusTraversalKeysEnabled(false);
			awtCanvas.setFocusTraversalKeysEnabled(false);
			
			awtCanvas.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
					// System.out.println("key typed on canvas");
					ClientCore.input.keyTyped(e.getKeyChar());
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					ClientCore.input.keyDown(e.getExtendedKeyCode());
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					// System.out.println("key released on canvas");
					ClientCore.input.keyUp(e.getExtendedKeyCode());
				}
			});
			
			// end the program when the window is closed; this way allows libGDX to shutdown correctly (I think...)
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("stopping gdx");
					canvas.exit();
					System.exit(0);
				}
			});
			
			uiFrame.addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(final WindowEvent e) {
					frame.setFocusableWindowState(true);
					ClientCore.input.reset(false);
				}
				
				@Override
				public void windowGainedFocus(final WindowEvent e) {
					frame.setFocusableWindowState(false);
					ClientCore.input.reset(true);
				}
			});
			
			uiFrame.setFocusable(true);
			
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
	
	private static JFrame makeUIFrame(JFrame frame) {
		JFrame uiFrame = new JFrame("UI Frame") {
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
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) { uiFrame.setVisible(false); uiFrame.dispose(); }
			
			@Override
			public void windowIconified(WindowEvent e) {
				uiFrame.setExtendedState(Frame.ICONIFIED);
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				uiFrame.setExtendedState(Frame.NORMAL);
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				uiFrame.setAlwaysOnTop(true);
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				uiFrame.setAlwaysOnTop(false);
			}
		});
		
		uiFrame.setUndecorated(true);
		uiFrame.setBackground(new Color(0, 0, 0, 0));
		uiFrame.setAlwaysOnTop(true);
		uiFrame.setType(Type.UTILITY);
		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				uiFrame.revalidate();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// System.out.println("moving over frame");
				uiFrame.setLocation(frame.getContentPane().getLocationOnScreen());
			}
			
			@Override
			public void componentShown(ComponentEvent e) {
				// System.out.println("showing over frame");
				uiFrame.setVisible(true);
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// System.out.println("hiding over frame");
				uiFrame.setVisible(false);
			}
		});
		
		return uiFrame;
	}
}
