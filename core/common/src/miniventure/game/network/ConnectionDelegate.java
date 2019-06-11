package miniventure.game.network;

import java.net.InetSocketAddress;

public interface ConnectionDelegate {
	
	@FunctionalInterface
	interface PacketListener {
		void receivePacket(Object packet);
	}
	
	void send(Object obj);
	
	InetSocketAddress getRemoteAddress();
	
}
