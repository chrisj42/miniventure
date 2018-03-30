package miniventure.game.screen;

import javax.swing.JOptionPane;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen {
	
	public MainMenu() {
		super();
		
		ClientWorld world = ClientCore.getWorld();
		
		addLabel("Miniventure", 20);
		addLabel("You are playing version " + GameCore.VERSION, 15);
		
		addLabel("Use mouse or arrow keys to move around.", 10);
		addLabel("C to attack, V to interact.", 10);
		addLabel("E to open your inventory, Z to craft items.", 10);
		addLabel("+ and - keys to zoom in and out.", 30);
		addLabel("Press \"t\" to chat with other players.", 30);
		//addLabel("(press b to show/hide chunk boundaries)", 30);
		//addLabel("", 10);
		
		VisTextButton button = new VisTextButton("Play");
		
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				world.createWorld(0, 0);
			}
		});
		
		vGroup.remove();
		addActor(table);
		table.add(button);
		
		VisTextButton joinBtn = new VisTextButton("Join Server");
		joinBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				String ipAddress = JOptionPane.showInputDialog("Enter the IP Address you want to connect to.");
				world.createWorld(ipAddress);
			}
		});
		
		table.row();
		table.add(joinBtn);
		
		table.setOrigin(Align.top);
		table.setPosition(getWidth()/2, getHeight()/2);
	}
	
	private void addLabel(String msg, int spacing) {
		table.add(new VisLabel(msg));
		table.row().space(spacing);
	}
}
