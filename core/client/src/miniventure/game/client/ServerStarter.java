package miniventure.game.client;

import miniventure.game.util.Action;

public interface ServerStarter {
	
	void startServer(int worldWidth, int worldHeight, Action callback);
	
}
