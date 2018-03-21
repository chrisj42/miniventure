package miniventure.game.world.entity.mob;

import miniventure.game.client.ClientWorld;
import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;

public class ClientMob extends Mob {
	
	public ClientMob(@NotNull ClientWorld world, @NotNull String spriteName, int health) {
		super(world, spriteName, health);
	}
	
}
