package miniventure.game.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import miniventure.game.world.management.SaveLoadInterface.WorldDataSet;

public interface ServerManager {
	
	// initialize static stuff and things.
	void init();
	
	// returns if a server is being run locally on the same JVM. Can be used to determine if the current world is a local single player one or not.
	boolean isHosting();
	
	// returns port server was started on
	int startServer(WorldDataSet worldInfo) throws IOException;
	void setHost(InetSocketAddress host); // tell the server who the host is
	void open(); // allow other players to join; TODO implement this option in a pause menu on client GUI
	void closeServer();
	
}
