package miniventure.game.screen;

import miniventure.game.core.ClientCore;
import miniventure.game.core.GameScreen;
import miniventure.game.screen.util.BackgroundProvider;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.level.ClientLevel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RespawnScreen extends BackgroundProvider {
	
	private final ClientPlayer deadPlayer;
	private final ClientLevel level;
	private final Color lighting;
	private final GameScreen backgroundRenderer;
	
	public RespawnScreen(ClientPlayer deadPlayer, Color lighting, GameScreen backgroundRenderer) {
		super(false, true, new ScreenViewport()); // level renderer clears it
		this.deadPlayer = deadPlayer;
		level = deadPlayer.getWorld().getLevel();
		this.lighting = lighting;
		this.backgroundRenderer = backgroundRenderer;
		
		VerticalGroup vGroup = useVGroup(20);
		
		vGroup.addActor(makeLabel("You died!"));
		vGroup.addActor(makeButton("Respawn", () -> ClientCore.getWorld().respawnPlayer()));
	}
	
	@Override
	public void renderBackground() {
		if(level != null)
			backgroundRenderer.render(lighting, level, false);
	}
	
	// background is level viewport of gamescreen; ClientCore always calls that directly, so there is no need to do it again here.
	@Override
	public void resizeBackground(int width, int height) {}
}
