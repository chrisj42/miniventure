package miniventure.game.server;

import com.esotericsoftware.kryonet.Connection;

@FunctionalInterface
public interface PacketHandler {
	
	void handle(Connection connection, Object object);
	
}
