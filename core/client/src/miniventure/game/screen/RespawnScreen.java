package miniventure.game.screen;

import miniventure.game.client.ClientCore;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import javax.swing.JLabel;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super(false);
		add(vGroup);
		
		add(new JLabel("You died!"));
		
		add(new JButton("Respawn", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ClientCore.getWorld().respawnPlayer();
			}
		}));
	}
}
