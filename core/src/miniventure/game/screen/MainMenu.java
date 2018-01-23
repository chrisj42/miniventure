package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen {
	
	public MainMenu() {
		super();
		
		table.add(new VisLabel("Miniventure"));
		table.row();
		VisTextButton button = new VisTextButton("Play");
		
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				GameCore.getWorld().createWorld();
			}
		});
		
		table.add(button);
	}
}
