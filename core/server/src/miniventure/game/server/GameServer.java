package miniventure.game.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miniventure.game.GameProtocol;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import org.jetbrains.annotations.NotNull;

public class GameServer implements GameProtocol {
	
	private final HashMap<Connection, Player> connectionToPlayerMap = new HashMap<>();
	private final HashMap<Player, Connection> playerToConnectionMap = new HashMap<>();
	
	private Server server;
	
	public GameServer() {
		server = new Server(writeBufferSize, objectBufferSize);
		GameProtocol.registerClasses(server.getKryo());
		
		addListener(new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ServerWorld world = ServerCore.getWorld();
				
				if(object instanceof Login) {
					System.out.println("server received login");
					
					// prepare level
					ServerLevel level = world.getLevel(0);
					if(level != null) {
						Player player = world.addPlayer();
						System.out.println("server player level: " + player.getLevel());
						connectionToPlayerMap.put(connection, player);
						playerToConnectionMap.put(player, connection);
						ChunkData[] playerChunks = level.createClientLevel(player);
						connection.sendTCP(new LevelData(level));
						for(ChunkData chunk: playerChunks)
							connection.sendTCP(chunk);
						connection.sendTCP(new SpawnData(player));
						System.out.println("server player level: " + player.getLevel());
					}
				}
				
				if(object instanceof ChunkRequest) {
					System.out.println("server received chunk request");
					ChunkRequest request = (ChunkRequest) object;
					ServerLevel level = world.getLevel(0);
					if(level != null) {
						Chunk chunk = level.getChunk(request.x, request.y);
						connection.sendTCP(new ChunkData(chunk, level));
						Array<Entity> newEntities = level.getOverlappingEntities(chunk.getBounds());
						for(Entity e: newEntities)
							connection.sendTCP(new EntityAddition(e));
					}
				}
				
				if(object instanceof Movement) {
					//System.out.println("server received movement");
					Movement m = (Movement) object;
					Player p = connectionToPlayerMap.get(connection);
					Level level = world.getLevel(m.levelDepth);
					//p.move(new Vector3(m.x, m.y, m.z).sub(new Vector3(p.getPosition(), p.getZ())));
					if(level != null)
						p.moveTo(level, m.x, m.y);
					//System.out.println("server has player at " + p.getPosition(true));
				}
				
				if(object instanceof InteractRequest) {
					System.out.println("server received interaction request");
					InteractRequest r = (InteractRequest) object;
					Player p = connectionToPlayerMap.get(connection);
					r.update.apply(p);
					if(r.attack) p.attack();
					else p.interact();
				}
				
				if(object.equals(DatalessRequest.Respawn)) {
					Player client = connectionToPlayerMap.get(connection);
					world.respawnPlayer(client);
					connection.sendTCP(new SpawnData(client));
				}
			}
			
			/*@Override
			public void connected(Connection connection) {
				System.out.println("new connection: " + connection.getRemoteAddressTCP().getHostString());
			}*/
			
			@Override
			public void disconnected(Connection connection) {
				Player player = connectionToPlayerMap.get(connection);
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
	
	public void sendToPlayer(@NotNull Player player, Object obj) {
		Connection c = playerToConnectionMap.get(player);
		if(c != null)
			c.sendTCP(obj);
	}
	
	// levelMask is the level a player must be on to receive this data.
	public void broadcast(Object obj, Level levelMask, @NotNull Player... exclude) {
		if(levelMask == null) return; // no level, no packet.
		
		Array<Player> players = new Array<>(exclude);
		for(Player p: playerToConnectionMap.keySet())
			if(levelMask.equals(p.getLevel()))
				players.add(p);
		
		for(Player p: exclude)
			players.removeValue(p, false);
		
		broadcast(obj, false, players.toArray(Player.class));
	}
	public void broadcast(Object obj, @NotNull Player... excludedPlayers) { broadcast(obj, false, excludedPlayers); }
	public void broadcast(Object obj, boolean includeGiven, @NotNull Player... players) {
		Set<Player> playerSet = new HashSet<>(playerToConnectionMap.keySet());
		List<Player> playerList = Arrays.asList(players);
		
		if(includeGiven)
			playerSet.retainAll(playerList);
		else
			playerSet.removeAll(playerList);
		
		for(Player p: playerSet)
			playerToConnectionMap.get(p).sendTCP(obj);
	}
	
	@Override
	public void sendData(Object obj) { broadcast(obj); }
}
