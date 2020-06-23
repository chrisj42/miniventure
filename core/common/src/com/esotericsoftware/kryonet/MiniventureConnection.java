package com.esotericsoftware.kryonet;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import miniventure.game.util.MyUtils;

public class MiniventureConnection extends Connection {
	
	private final LinkedList<Object> sendQueue = new LinkedList<>();
	private final Object queueLock = new Object();
	
	protected MiniventureConnection() {
		super();
		addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				setIdleThreshold(0.5f);
			}
			
			@Override
			public void idle(Connection connection) {
				synchronized (queueLock) {
					if(sendQueue.size() == 0)
						return;
					
					while(isIdle() && sendQueue.size() > 0)
						MiniventureConnection.super.sendTCP(sendQueue.pollFirst());
				}
			}
		});
	}
	
	public InetSocketAddress getLocalAddressTCP() {
		return getLocalAddressTCP(this);
	}
	
	public static InetSocketAddress getLocalAddressTCP(Connection connection) {
		SocketChannel socketChannel = connection.tcp.socketChannel;
		if (socketChannel != null) {
			Socket socket = connection.tcp.socketChannel.socket();
			if (socket != null) {
				return (InetSocketAddress)socket.getLocalSocketAddress();
			}
		}
		return null;
	}
	
	private int maxSize = 0;
	
	@Override
	public int sendTCP(Object object) {
		synchronized (queueLock) {
			if(sendQueue.size() > 0 || !isIdle()) {
				sendQueue.addLast(object);
				return 0;
			}
		}
		
		int sent = super.sendTCP(object);
		if(sent > maxSize) {
			MyUtils.debug("new largest packet of "+sent+" bytes is of type "+object.getClass().getSimpleName());
			maxSize = sent;
		}
		if(tcp.writeBuffer.position() > 0)
			MyUtils.debug("write buffer usage: "+(tcp.writeBuffer.position()/* / (float) tcp.writeBuffer.capacity()*/));
		/*if(tcp.writeBuffer.position() > 7_000) {
			tcp.socketChannel.
			try {
				tcp.writeOperation();
			} catch (IOException e) {
				GameCore.debug("failed to write buffer: "+e.getMessage());
			}
			GameCore.debug("new write buffer usage: "+(tcp.writeBuffer.position()*//* / (float) tcp.writeBuffer.capacity()*//*));
		}*/
		return sent;
	}
}
