package miniventure.game.desktop;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.Canvas;
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
			LwjglCanvas canvas = new LwjglCanvas(new ClientCore((width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
			
			// now make all the swing components.
			
			
			final Dimension WINDOW_SIZE = new Dimension(config.width, config.height);
			
			// the frame
			JFrame frame = new JFrame(config.title);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.getContentPane().setLayout(null);
			frame.getContentPane().setPreferredSize(WINDOW_SIZE);
			
			
			// the canvas where libGDX rendering occurs
			Canvas awtCanvas = canvas.getCanvas();
			// awtCanvas.setPreferredSize(WINDOW_SIZE);
			awtCanvas.setSize(WINDOW_SIZE);
			
			// the panel where swing UI components will be added.
			JPanel uiPanel = new JPanel();
			com.sun.awt.AWTUtilities.setComponentMixingCutoutShape(uiPanel, new Rectangle());
			// uiPanel.setPreferredSize(WINDOW_SIZE);
			uiPanel.setSize(WINDOW_SIZE);
			uiPanel.setOpaque(false);
			uiPanel.setBackground(null);
			
			frame.add(uiPanel);
			frame.add(awtCanvas);
			
			// have the canvas and UI Panel track and match the frame size.
			frame.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					SwingUtilities.invokeLater(() -> {
						Dimension size = frame.getContentPane().getSize();
						awtCanvas.setSize(size);
						uiPanel.setSize(size);
					});
				}
			});
			
			// end the program when the window is closed
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("stopping gdx");
					canvas.exit();
				}
			});
			
			
			// add a sample button to the ui panel
			
			JButton dialog = new JButton("open dialog");
			dialog.addActionListener(e -> JOptionPane.showMessageDialog(null, "Hi!"));
			
			uiPanel.add(dialog);
			
			
			// run the program in the event thread
			SwingUtilities.invokeLater(() -> {
				frame.pack();
				frame.setVisible(true);
			});
		}
	}
}
