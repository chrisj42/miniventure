package miniventure.game.screen;

import miniventure.game.world.entity.mob.player.Player;

import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.jetbrains.annotations.NotNull;

public class RespawnScreen extends MenuScreen {
	
	// private final Player deadPlayer;
	// private final Level level;
	// private final Color lighting;
	// private final GameScreen backgroundRenderer;
	
	public RespawnScreen(@NotNull Player player) {
		super(new ScreenViewport()); // level renderer clears it
		// this.deadPlayer = deadPlayer;
		// level = deadPlayer.getWorld().getLevel();
		// this.lighting = lighting;
		// this.backgroundRenderer = backgroundRenderer;
		
		VerticalGroup vGroup = useVGroup(20);
		
		vGroup.addActor(makeLabel("You died!"));
		vGroup.addActor(makeButton("Respawn", () -> player.getWorld().respawnPlayer()));
	}
}
