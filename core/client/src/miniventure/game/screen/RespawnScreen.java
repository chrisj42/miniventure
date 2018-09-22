package miniventure.game.screen;

import javax.swing.JLabel;

import miniventure.game.client.ClientCore;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super(true, false);
		// add(vGroup);
		
		addCentered(new JLabel("You died!"));
		addCentered(makeButton("Respawn", () -> ClientCore.getWorld().respawnPlayer()));
	}
	
}
