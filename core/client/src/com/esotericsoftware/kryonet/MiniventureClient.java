package com.esotericsoftware.kryonet;

import java.net.InetSocketAddress;

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
}
