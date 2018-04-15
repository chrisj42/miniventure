package miniventure.game.screen;

import javax.swing.JOptionPane;

import miniventure.game.GameCore;
import miniventure.game.client.ClientCore;
import miniventure.game.client.ClientWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen {
	
	private boolean dialog = false;
	
	public MainMenu() {
		super();
		
		ClientWorld world = ClientCore.getWorld();
		
		addLabel("Miniventure", 20);
		addLabel("You are playing version " + GameCore.VERSION, 15);
		
		addLabel("Use mouse or arrow keys to move around.", 10);
		addLabel("C to attack, V to interact.", 10);
		addLabel("E to open your inventory, Z to craft items.", 10);
		addLabel("+ and - keys to zoom in and out.", 10);
		addLabel("Press \"t\" to chat with other players, and \"/\" to use commands.", 0);
		addLabel("(Hint: use up arrow key in chat screen to access previous entries and avoid retyping things.)", 30);
		//addLabel("(press b to show/hide chunk boundaries)", 30);
		//addLabel("", 10);
		
		VisTextButton button = new VisTextButton("Play");
		
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				if(dialog) return;
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
				if(dialog) return;
				dialog = true;
				LoadingScreen loader = new LoadingScreen();
				loader.pushMessage("Preparing to connect...");
				ClientCore.setScreen(loader);
				new Thread(() -> {
					String ipAddress = JOptionPane.showInputDialog("Enter the IP Address you want to connect to.");
					Gdx.app.postRunnable(() -> {
						if(ipAddress != null)
							world.createWorld(ipAddress);
						else
							ClientCore.setScreen(new MainMenu());
					});
					dialog = false;
				}).start();
			}
		});
		
		table.row();
		table.add(joinBtn);
		
		table.setOrigin(Align.top);
		table.setPosition(getWidth()/2, getHeight()/2);
	}
	
	@Override
	public void draw() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		super.draw();
	}
		
		private void addLabel(String msg, int spacing) {
		table.add(new VisLabel(msg));
		table.row().space(spacing);
	}
}
