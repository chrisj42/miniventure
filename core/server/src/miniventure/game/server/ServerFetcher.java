package miniventure.game.server;

import java.io.IOException;

import miniventure.game.world.file.PlayerData;
import miniventure.game.world.management.ServerWorld;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ServerFetcher {
	
	@NotNull
	GameServer get(@NotNull ServerWorld world, PlayerData[] playerData) throws IOException;
	
}
