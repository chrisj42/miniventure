package miniventure.server;

import java.io.IOException;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.Login;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class GameServer {
	
	private final HashMap<Connection, Player> playerConnections = new HashMap<>();
	
	private Server server;
	
	public GameServer() {
		server = new Server();
		GameProtocol.registerClasses(server.getKryo());
		
		addListener(new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				if(object instanceof Login) {
					System.out.println("server received login");
					
					// prepare level
					ServerLevel level = ServerLevel.getLevel(0);
					if(level != null) {
						Player player = ServerCore.getWorld().addPlayer();
						playerConnections.put(connection, player);
						LevelData playerLevel = level.createClientLevel(player);
						connection.sendTCP(playerLevel);
						Vector2 pos = player.getPosition();
						connection.sendTCP(new SpawnData(pos.x, pos.y));
					}
				}
			}
			
			/*@Override
			public void connected(Connection connection) {
				System.out.println("new connection: " + connection.getRemoteAddressTCP().getHostString());
			}*/
			
			@Override
			public void disconnected(Connection connection) {
				Player player = playerConnections.get(connection);
				if(player != null)
					player.remove();
				
				//System.out.println("client disconnected: " + connection.getRemoteAddressTCP().getHostString());
			}
		});
		
		server.start();
	}
	
	public void addListener(Listener listener) { server.addListener(listener); }
	
	public boolean startServer() { return startServer(GameProtocol.PORT); }
	public boolean startServer(int port) {
		try {
			server.bind(port);
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
