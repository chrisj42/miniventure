package miniventure.game.screen;

import miniventure.game.core.ClientCore;

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RespawnScreen extends MenuScreen {
	
	public RespawnScreen() {
		super(new ScreenViewport()); // level renderer clears it
		
		VerticalGroup vGroup = useVGroup(20);
		
		vGroup.addActor(makeLabel("You died!"));
		vGroup.addActor(makeButton("Respawn", () -> ClientCore.getWorld().requestRespawn()));
	}
	
}
