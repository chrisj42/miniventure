package miniventure.game.client;

import miniventure.game.world.SaveLoadInterface.WorldDataSet;

public interface ServerManager {
	
	boolean startServer(WorldDataSet worldInfo);
	void closeServer();
	
}
