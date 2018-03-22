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
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import org.jetbrains.annotations.NotNull;

public class GameServer implements GameProtocol {
	
	private final HashMap<Connection, ServerPlayer> connectionToPlayerMap = new HashMap<>();
	private final HashMap<ServerPlayer, Connection> playerToConnectionMap = new HashMap<>();
	
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
						ServerPlayer player = world.addPlayer();
						System.out.println("server player level: " + player.getLevel());
						connectionToPlayerMap.put(connection, player);
						playerToConnectionMap.put(player, connection);
						ChunkData[] playerChunks = level.createClientLevel(player);
						connection.sendTCP(new LevelData(level));
						for(ChunkData chunk: playerChunks)
							connection.sendTCP(chunk);
						connection.sendTCP(new SpawnData(new EntityAddition(player), player));
						System.out.println("server player level: " + player.getLevel());
					}
					return;
				}
				
				ServerPlayer p = connectionToPlayerMap.get(connection);
				
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
				
				if(object instanceof PositionUpdate) {
					//System.out.println("server received movement");
					PositionUpdate m = (PositionUpdate) object;
					Level level = world.getLevel(m.levelDepth);
					//p.move(new Vector3(m.x, m.y, m.z).sub(new Vector3(p.getPosition(), p.getZ())));
					if(level != null)
						p.moveTo(level, m.x, m.y);
					//System.out.println("server has player at " + p.getPosition(true));
				}
				
				if(object instanceof InteractRequest) {
					System.out.println("server received interaction request");
					InteractRequest r = (InteractRequest) object;
					if(r.playerPosition.variesFrom(p))
						connection.sendTCP(new PositionUpdate(p)); // fix the player's position
					if(r.attack) p.attack();
					else p.interact();
				}
				
				forPacket(object, PlayerMovement.class, r -> {
					p.moveInDir(new Vector2(r.xdir, r.ydir));
					
				});
				
				forPacket(object, ItemDropRequest.class, drop -> {
					int removed = p.getInventory().removeItem(drop.stack);
					ServerLevel level = p.getLevel();
					if(level == null) return;
					for(int i = 0; i < removed; i++)
						level.dropItem(drop.stack.item.copy(), p.getPosition(), null);
				});
				
				forPacket(object, HeldItemRequest.class, handItem -> {
					p.getHands().clearItem(p.getInventory());
					int count = p.getInventory().removeItem(handItem.stack);
					if(count > 0)
						p.getHands().setItem(handItem.stack.item, count);
				});
				
				if(object.equals(DatalessRequest.Respawn)) {
					ServerPlayer client = connectionToPlayerMap.get(connection);
					world.respawnPlayer(client);
					connection.sendTCP(new SpawnData(new EntityAddition(client), client));
				}
			}
			
			/*@Override
			public void connected(Connection connection) {
				System.out.println("new connection: " + connection.getRemoteAddressTCP().getHostString());
			}*/
			
			@Override
			public void disconnected(Connection connection) {
				ServerPlayer player = connectionToPlayerMap.get(connection);
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
	
	public void sendToPlayer(@NotNull ServerPlayer player, Object obj) {
		Connection c = playerToConnectionMap.get(player);
		if(c != null)
			c.sendTCP(obj);
	}
	
	// levelMask is the level a player must be on to receive this data.
	public void broadcast(Object obj, Level levelMask, @NotNull ServerPlayer... exclude) {
		if(levelMask == null) return; // no level, no packet.
		
		Array<ServerPlayer> players = new Array<>(exclude);
		for(ServerPlayer p: playerToConnectionMap.keySet())
			if(levelMask.equals(p.getLevel()))
				players.add(p);
		
		for(ServerPlayer p: exclude)
			players.removeValue(p, false);
		
		broadcast(obj, false, players.toArray(ServerPlayer.class));
	}
	public void broadcast(Object obj, @NotNull ServerPlayer... excludedPlayers) { broadcast(obj, false, excludedPlayers); }
	public void broadcast(Object obj, boolean includeGiven, @NotNull ServerPlayer... players) {
		Set<ServerPlayer> playerSet = new HashSet<>(playerToConnectionMap.keySet());
		List<ServerPlayer> playerList = Arrays.asList(players);
		
		if(includeGiven)
			playerSet.retainAll(playerList);
		else
			playerSet.removeAll(playerList);
		
		for(ServerPlayer p: playerSet)
			playerToConnectionMap.get(p).sendTCP(obj);
	}
}
