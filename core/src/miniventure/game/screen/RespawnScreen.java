package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super();
		
		table.add(new VisLabel("You died!"));
		table.row();
		table.add(new VisTextButton("Respawn", new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameCore.getWorld().respawn();
				GameCore.setScreen(null);
			}
		}));
	}
}
