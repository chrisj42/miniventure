package miniventure.game.client;

import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.worldgen.WorldConfig;

public interface ServerManager {
	
	void startServer(WorldConfig config, final ValueFunction<Boolean> callback);
	void closeServer();
	
}
