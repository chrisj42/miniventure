package miniventure.game.client;

import miniventure.game.world.WorldFile;

public interface ServerManager {
	
	boolean createWorld(WorldFile worldFile);
	
	boolean startServer(WorldFile worldFile);
	void closeServer();
	
}
