package miniventure.game.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import miniventure.game.world.SaveLoadInterface.WorldDataSet;

public interface ServerManager {
	
	// returns port server was started on
	int startServer(WorldDataSet worldInfo) throws IOException;
	void setHost(InetSocketAddress host); // tell the server who the host is
	void open(); // allow other players to join; TODO implement this option in a pause menu on client GUI
	void closeServer();
	
}
