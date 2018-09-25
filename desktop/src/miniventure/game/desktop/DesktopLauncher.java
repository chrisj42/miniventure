package miniventure.game.desktop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.screen.ClientUtils;
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
			frame.getContentPane().setLayout(null); // manual positioning
			frame.getContentPane().setPreferredSize(WINDOW_SIZE);
			
			// the panel where swing UI components will be added (i.e. menu screens):
			JPanel hudPanel = makeUIPanel(frame, WINDOW_SIZE);
			
			// the panel where swing HUD components will be added (i.e. health/hunger bars, debug display, chat overlay):
			JPanel uiPanel = makeUIPanel(frame, WINDOW_SIZE);
			
			LwjglCanvas canvas = new LwjglCanvas(new ClientCore(hudPanel, uiPanel, (width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
			
			
			// the canvas where libGDX rendering occurs
			Canvas awtCanvas = canvas.getCanvas();
			ClientUtils.trackParentSize(awtCanvas, frame.getContentPane(), WINDOW_SIZE);
			awtCanvas.setFocusable(true);
			awtCanvas.requestFocusInWindow();
			
			// end the program when the window is closed; this way allows libGDX to shutdown correctly (I think...)
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("stopping gdx");
					canvas.exit();
					System.exit(0);
				}
			});
			
			
			/*frame.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_L) {
						if(e.isShiftDown()) {
							System.out.println("canvas request focus in window");
							awtCanvas.requestFocusInWindow();
						} else {
							System.out.println("canvas request focus");
							awtCanvas.requestFocus();
						}
					}
				}
			});*/
			
			/*KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", e -> {
				Object oldVal = e.getOldValue();
				Object newVal = e.getNewValue();
				String oldStr = oldVal == null ? null : oldVal.getClass().getName();
				if(oldStr != null)
					oldStr = oldStr.substring(oldStr.lastIndexOf(".")+1)+"@"+oldVal.hashCode();
				String newStr = newVal == null ? null : newVal.getClass().getName();
				if(newStr != null)
					newStr = newStr.substring(newStr.lastIndexOf(".")+1)+"@"+newVal.hashCode();
				System.out.println("keyboard focus changed from "+oldStr+" to "+newStr);
				if(newStr != null && newVal instanceof Component)
					System.out.println("new component visible: "+((Component)newVal).isVisible());
			});*/
			
			// run the program in the event thread
			SwingUtilities.invokeLater(() -> {
				frame.pack();
				frame.setVisible(true);
			});
		}
	}
	
	private static JPanel makeUIPanel(JFrame frame, Dimension size) {
		JPanel p = new JPanel(null) {
			@Override
			public Dimension getPreferredSize() {
				return frame.getContentPane().getPreferredSize();
			}
			
			@Override
			public Dimension getMinimumSize() {
				return frame.getContentPane().getMinimumSize();
			}
			
			@Override
			public Dimension getMaximumSize() {
				return frame.getContentPane().getMaximumSize();
			}
		};
		
		p.setFocusable(false);
		p.setOpaque(false);
		ClientUtils.setupTransparentAWTContainer(p);
		ClientUtils.trackParentSize(p, frame.getContentPane(), size);
		// frame.getContentPane().add(p);
		
		return p;
	}
}
