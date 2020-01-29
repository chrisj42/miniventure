package com.esotericsoftware.kryonet;

import miniventure.game.core.GameCore;

public class MiniventureServer extends Server {
	
	public MiniventureServer() {
		super();
	}
	
	public MiniventureServer(int writeBufferSize, int objectBufferSize) {
		super(writeBufferSize, objectBufferSize);
	}
	
	public MiniventureServer(int writeBufferSize, int objectBufferSize, Serialization serialization) {
		super(writeBufferSize, objectBufferSize, serialization);
	}
	
	@Override
	public void start() {
		Thread thread = new Thread(this, "Server");
		thread.setUncaughtExceptionHandler((t, ex) -> {
			t.getThreadGroup().uncaughtException(t, ex);
			close();
		});
		thread.start();
	}
	
	@Override
	public void run() {
		GameCore.debug("Starting Network server update thread");
		super.run();
		GameCore.debug("Ending Network server update thread");
	}
	
	@Override
	protected Connection newConnection() {
		return new MiniventureConnection();
	}
}
