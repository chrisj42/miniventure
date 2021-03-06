package com.esotericsoftware.kryonet;

import java.net.InetSocketAddress;

import miniventure.game.util.MyUtils;

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
		MyUtils.debug("Starting Network client update thread");
		super.run();
		MyUtils.debug("Ending Network client update thread");
	}
	
	private int maxSize = 0;
	
	@Override
	public int sendTCP(Object object) {
		int sent = super.sendTCP(object);
		
		if(sent > maxSize) {
			MyUtils.debug("new largest packet of "+sent+" bytes is of type "+object.getClass().getSimpleName());
			maxSize = sent;
		}
		return sent;
	}
}
