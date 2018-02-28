package miniventure.server;

import java.io.IOException;

import miniventure.game.GameProtocol;
import miniventure.game.GameProtocol.Login;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.mob.Player;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class GameServer extends Listener {
	
	private Server server;
	private ServerWorld world;
	
	public GameServer(ServerWorld world) { this(world, GameProtocol.PORT); }
	public GameServer(ServerWorld world, int port) {
		this.world = world;
		
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
		if(object instanceof Login) {
			System.out.println("server received login");
			
			// prepare level
			ServerLevel level = ServerLevel.getLevel(0);
			if(level != null) {
				Player player = world.spawnPlayer(connection);
				Level playerLevel = level.createClientLevel(player);
				connection.sendTCP(playerLevel);
				connection.sendTCP(player);
			}
		}
		
		// login packet to start, send back level, and then player
		
	}
	
	@Override
	public void connected(Connection connection) {
		// log player
		
		/*
			So, player logs in, they get..?
		 */
	}
	
	
}
