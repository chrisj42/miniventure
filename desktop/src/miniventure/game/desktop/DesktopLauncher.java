package miniventure.game.desktop;

import javax.swing.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.server.ServerCore;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	
	/**
	 * Main method for run the application
	 * 
	 * @param args arguments of application
	 */
	public static void main (String[] args) {
		/*
		Based on the below experiment, I'm going to try and size text labels lazily, by having them have a default preferred size of one line, and then if later the width ends up being too small to fit then the preferred size changes to fit it. So it will always lag behind.
		
		JLabel label = new JLabel("<html><div><p style=\"color:red\">This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label.</p></div>");
		JLabel label2 = new JLabel("<html><div><p style=\"color:blue\">This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label. This is a label.</p></div>");
		printSizes(label);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		// panel.setLayout(new GridLayout());
		// panel.setLayout(new FlowLayout());
		panel.setPreferredSize(new Dimension(300, 800));
		panel.add(label);
		panel.add(label2);
		// panel.add(Box.createHorizontalStrut(10));
		// panel.add(Box.createVerticalStrut(10));
		// panel.add(Box.createVerticalStrut(50));
		printSizes(label);
		label.revalidate();
		printSizes(label);
		
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				printSizes(label);
			}
		});
		
		JFrame frame = new JFrame("frame test");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		printSizes(label);
		frame.setVisible(true);
		printSizes(label);
		SwingUtilities.invokeLater(() -> printSizes(label));
		
		//noinspection ConstantConditions
		if(true) return;
		*/
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
			new LwjglApplication(new ClientCore((width, height, callback) -> {
				ServerCore.initServer(width, height, false);
				// server running, and world loaded; now, get the server world updating
				new Thread(new ThreadGroup("server"), ServerCore::run, "Miniventure Server").start();
				callback.act(); // ready to connect
				
			}), config);
		}
	}
	
	private static void printSizes(Component c) {
		System.out.println(c.getMinimumSize());
		System.out.println(c.getPreferredSize());
		System.out.println(c.getMaximumSize());
		System.out.println(c.getSize());
		System.out.println();
		
	}
}
