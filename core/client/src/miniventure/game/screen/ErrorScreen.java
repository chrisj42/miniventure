package miniventure.game.screen;

import miniventure.game.client.ClientCore;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ErrorScreen extends MenuScreen {
	
	public ErrorScreen(String error) {
		
		addActor(vGroup);
		
		vGroup.addActor(new VisLabel(error));
		
		vGroup.space(50);
		
		
		VisTextButton retryBtn = new VisTextButton("Reconnect");
		retryBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				ClientCore.getWorld().rejoinWorld();
			}
		});
		vGroup.addActor(retryBtn);
		
		vGroup.space(10);
		
		VisTextButton returnBtn = new VisTextButton("Back to main menu");
		returnBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent e, float x, float y) {
				ClientCore.setScreen(new MainMenu());
			}
		});
		vGroup.addActor(returnBtn);
	} 
	
	@Override public void focus() { ClientCore.stopMusic(); }
}
