package miniventure.game.client;

import miniventure.game.util.function.MonoVoidFunction;

public interface ServerManager {
	
	void startServer(int worldWidth, int worldHeight, final MonoVoidFunction<Boolean> callback);
	void closeServer();
	
}
