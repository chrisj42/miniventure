package miniventure.game.desktop;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
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
			JPanel uiPanel = makeUIPanel(frame, WINDOW_SIZE);
			uiPanel.setLayout(new BorderLayout());
			
			// the panel where swing HUD components will be added (i.e. health/hunger bars, debug display, chat overlay):
			JPanel hudPanel = makeUIPanel(frame, WINDOW_SIZE);
			hudPanel.setLayout(null);
			
			LwjglCanvas canvas = new LwjglCanvas(new ClientCore(hudPanel, uiPanel, (width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
			
			
			// the canvas where libGDX rendering occurs
			Canvas awtCanvas = canvas.getCanvas();
			trackFrameSize(awtCanvas, frame, WINDOW_SIZE);
			
			// end the program when the window is closed; this way allows libGDX to shutdown correctly (I think...)
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("stopping gdx");
					canvas.exit();
				}
			});
			
			
			// add a sample button to the ui panel
			
			// JButton dialog = new JButton("open dialog");
			// dialog.addActionListener(e -> JOptionPane.showMessageDialog(null, "Hi!"));
			//
			// uiPanel.add(dialog);
			
			
			// run the program in the event thread
			SwingUtilities.invokeLater(() -> {
				frame.pack();
				frame.setVisible(true);
			});
		}
	}
	
	private static JPanel makeUIPanel(JFrame frame, Dimension size) {
		JPanel p = new JPanel() {
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
		
		ClientCore.setupTransparentSwingContainer(p, false);
		trackFrameSize(p, frame, size);
		
		return p;
	}
	
	// adds a listener to the frame to make sure the given component size always fills the entire space inside the frame. Starts at the given size.
	private static void trackFrameSize(Component c, JFrame frame, Dimension size) {
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				SwingUtilities.invokeLater(() -> c.setSize(frame.getContentPane().getSize()));
			}
		});
		
		c.setSize(size);
		frame.add(c);
	}
}
