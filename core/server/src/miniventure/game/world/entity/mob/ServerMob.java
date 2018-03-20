package miniventure.game.world.entity.mob;

import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;

public class ServerMob extends Mob {
	
	public ServerMob(@NotNull WorldManager world, @NotNull String spriteName, int health) {
		super(world, spriteName, health);
	}
	
}
