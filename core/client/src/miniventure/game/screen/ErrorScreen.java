package miniventure.game.screen;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import miniventure.game.client.ClientCore;

public class ErrorScreen extends MenuScreen {
	
	public ErrorScreen(String error) {
		
		// add(vGroup);
		
		super(true, false);
		add(new JLabel(error));
		
		add(Box.createVerticalStrut(50));
		
		
		JButton retryBtn = new JButton("Reconnect");
		retryBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ClientCore.getWorld().rejoinWorld();
			}
		});
		add(retryBtn);
		
		add(Box.createVerticalStrut(10));
		
		JButton returnBtn = new JButton("Back to main menu");
		returnBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ClientCore.setScreen(new MainMenu());
			}
		});
		add(returnBtn);
	} 
	
	@Override public void focus() { ClientCore.stopMusic(); super.focus(); }
}
