package miniventure.game.client;

import miniventure.game.util.function.ValueFunction;

public interface ServerManager {
	
	void startServer(int worldWidth, int worldHeight, final ValueFunction<Boolean> callback);
	void closeServer();
	
}
