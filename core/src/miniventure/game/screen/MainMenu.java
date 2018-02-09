package miniventure.game.screen;

import miniventure.game.GameCore;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen {
	
	public MainMenu() {
		super();
		
		addLabel("Miniventure", 20);
		addLabel("You are playing version " + GameCore.VERSION, 15);
		
		addLabel("Use mouse or arrow keys to move around.", 10);
		addLabel("C to attack, V to interact.", 10);
		addLabel("E to open your inventory, Z to craft items.", 30);
		//addLabel("", 10);
		
		VisTextButton button = new VisTextButton("Play");
		
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				GameCore.getWorld().createWorld(0, 0);
			}
		});
		
		vGroup.remove();
		addActor(table);
		table.add(button);
	}
	
	private void addLabel(String msg, int spacing) {
		table.add(new VisLabel(msg));
		table.row().space(spacing);
	}
}
