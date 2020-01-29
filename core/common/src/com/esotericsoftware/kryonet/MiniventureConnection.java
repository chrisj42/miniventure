package com.esotericsoftware.kryonet;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import miniventure.game.GameCore;

public class MiniventureConnection extends Connection {
	
	protected MiniventureConnection() {
		super();
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
		int sent = super.sendTCP(object);
		if(sent > maxSize) {
			GameCore.debug("new largest packet of "+sent+" bytes is of type "+object.getClass().getSimpleName());
			maxSize = sent;
		}
		return sent;
	}
}
