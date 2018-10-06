package miniventure.game.screen;

import miniventure.game.client.ClientCore;
import miniventure.game.client.LevelViewport;
import miniventure.game.world.ClientLevel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;

public class RespawnScreen extends BackgroundProvider {
	
	private final Vector2 deathPos;
	private final Color lighting;
	private final ClientLevel level;
	private final LevelViewport backgroundRenderer;
	
	public RespawnScreen(Vector2 deathPos, Color lighting, ClientLevel level, LevelViewport backgroundRenderer) {
		super(false, true); // level renderer clears it
		this.deathPos = deathPos;
		this.lighting = lighting;
		this.level = level;
		this.backgroundRenderer = backgroundRenderer;
		
		VerticalGroup vGroup = useVGroup(20);
		
		vGroup.addActor(makeLabel("You died!"));
		vGroup.addActor(makeButton("Respawn", () -> ClientCore.getWorld().respawnPlayer()));
	}
	
	@Override
	public void renderBackground() {
		if(level != null)
			backgroundRenderer.render(deathPos, lighting, level);
	}
	
	@Override
	public void resizeBackground(int width, int height) {
		backgroundRenderer.resize(width, height);
	}
}
