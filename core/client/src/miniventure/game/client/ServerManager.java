package miniventure.game.client;

import miniventure.game.file.WorldFile;
import miniventure.game.util.function.ValueFunction;

public interface ServerManager {
	
	void startServer(WorldFile worldFile, final ValueFunction<Boolean> callback);
	void closeServer();
	
}
