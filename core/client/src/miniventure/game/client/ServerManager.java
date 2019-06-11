package miniventure.game.client;

import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.file.WorldDataSet;

public interface ServerManager {
	
	// initialize static stuff and things.
	void init();
	
	// returns if a server is being run locally on the same JVM. Can be used to determine if the current world is a local single player one or not.
	boolean isHosting();
	
	// save the world to file.
	void save();
	
	boolean startSPServer(WorldDataSet worldInfo, PacketPipeReader serverIn, PacketPipeWriter serverOut, ProgressLogger logger);
	
	void startMPServer(NetworkClient netClient, WorldDataSet worldInfo, LoadingScreen logger, ValueAction<Boolean> callback);
	
	void closeServer();
	
}
