package miniventure.game.client;

import java.io.IOException;

import miniventure.game.world.SaveLoadInterface.WorldDataSet;

public interface ServerManager {
	
	// returns port server was started on
	int startServer(WorldDataSet worldInfo) throws IOException;
	void closeServer();
	void open(); // allow other players to join; TODO implement this option in a pause menu on client GUI
	
}
