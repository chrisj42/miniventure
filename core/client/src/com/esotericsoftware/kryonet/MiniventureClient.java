package com.esotericsoftware.kryonet;

import java.net.InetSocketAddress;

import miniventure.game.GameCore;

public class MiniventureClient extends Client {
	
	public MiniventureClient() {
		super();
	}
	
	public MiniventureClient(int writeBufferSize, int objectBufferSize) {
		super(writeBufferSize, objectBufferSize);
	}
	
	public MiniventureClient(int writeBufferSize, int objectBufferSize, Serialization serialization) {
		super(writeBufferSize, objectBufferSize, serialization);
	}
	
	public InetSocketAddress getLocalAddressTCP() {
		return MiniventureConnection.getLocalAddressTCP(this);
	}
	
	@Override
	public void run() {
		GameCore.debug("Starting Network client update thread");
		super.run();
		GameCore.debug("Ending Network client update thread");
	}
}
