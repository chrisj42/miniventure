package miniventure.game.network;

import miniventure.game.network.PacketPipe.PacketPipeWriter;

@FunctionalInterface
public interface PacketHandler<T> {
	
	void handlePacket(PacketPipeWriter connection, T packet);
	
}
