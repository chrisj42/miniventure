package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.screen.util.BackgroundInheritor;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class PauseScreen extends BackgroundInheritor {
	
	public PauseScreen() {
		Table table = useTable();
		
		table.defaults().pad(20);
		
		VisTextButton resume = makeButton("Resume", () -> ClientCore.setScreen(null));
		table.add(resume).row();
		
		VisTextButton save = makeButton("Save World", () -> {
			ClientCore.getWorld().saveWorld();
			ClientCore.setScreen(null);
		});
		if(ClientCore.getWorld().isLocalWorld())
			table.add(save).row();
		
		VisTextButton exit = makeButton("Main Menu", () -> ClientCore.setScreen(new ConfirmScreen(ClientCore.getWorld().isLocalWorld() ? "Exit World?" : "Leave Server?", () -> ClientCore.getWorld().exitWorld())));
		table.add(exit).row();
		
		table.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchDown);
					resume.fire(event1);
					setKeyboardFocus(table);
					return true;
				}
				return false;
			}
			
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Keys.ENTER) {
					InputEvent event1 = new InputEvent();
					event1.setType(Type.touchUp);
					resume.fire(event1);
					return true;
				}
				return false;
			}
		});
		
		setKeyboardFocus(table);
	}
	
}
