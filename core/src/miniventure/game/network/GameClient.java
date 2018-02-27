package miniventure.game.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class GameClient extends Listener {
	
	private Client client;
	
	public GameClient(String host) { this(host, GameProtocol.PORT); }
	public GameClient(String host, int port) {
		client = new Client();
		GameProtocol.registerClasses(client.getKryo());
		client.start();
		try {
			client.connect(5000, host, port);
			client.addListener(this);
			client.sendTCP(new SendObject("i am a client"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void received (Connection connection, Object object) {
		if(object instanceof SendObject) {
			System.out.println("client received " + ((SendObject)object).data);
		}
	}
}
