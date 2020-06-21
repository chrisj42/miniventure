package miniventure.game.network;

import java.io.IOException;

import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ServerFetcher {
	
	@NotNull
	GameServer get(@NotNull WorldManager world, PlayerData[] playerData) throws IOException;
	
}
