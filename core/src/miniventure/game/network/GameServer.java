package miniventure.game.network;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class GameServer extends Listener {
	
	private Server server;
	
	public GameServer() { this(GameProtocol.PORT); }
	public GameServer(int port) {
		server = new Server();
		GameProtocol.registerClasses(server.getKryo());
		server.start();
		
		try {
			server.bind(port);
			server.addListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void received (Connection connection, Object object) {
		if(object instanceof SendObject) {
			System.out.println("server received " + ((SendObject)object).data);
			connection.sendTCP(new SendObject("data was received"));
		}
	}
}
