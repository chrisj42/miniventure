package miniventure.game.server;

import java.util.LinkedList;

import miniventure.game.util.MyUtils;

import com.esotericsoftware.kryonet.Connection;

import org.jetbrains.annotations.NotNull;

class ServerThread extends Thread {
	
	private final Connection connection;
	private final PacketHandler packetHandler;
	
	private final LinkedList<Object> packetQueue = new LinkedList<>();
	private final Object queueLock = new Object();
	
	private boolean run = true;
	
	ServerThread(@NotNull Connection connection, @NotNull PacketHandler packetHandler) {
		this.connection = connection;
		this.packetHandler = packetHandler;
	}
	
	void addPacket(Object p) {
		synchronized (queueLock) { packetQueue.add(p); }
	}
	
	private Object next() {
		synchronized (queueLock) { return packetQueue.pollFirst(); }
	}
	
	void end() { run = false; }
	
	@Override
	public void run() {
		while(run && connection.isConnected()) {
			Object packet = next();
			
			if(packet != null)
				packetHandler.handle(connection, packet);
			else
				MyUtils.sleep(3);
		}
	}
}
