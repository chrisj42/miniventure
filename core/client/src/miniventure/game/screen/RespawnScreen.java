package miniventure.game.screen;

import miniventure.game.client.ClientCore;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super();
		
		vGroup.addActor(new VisLabel("You died!"));
		
		vGroup.addActor(new VisTextButton("Respawn", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ClientCore.getWorld().respawnPlayer();
			}
		}));
	}
}
