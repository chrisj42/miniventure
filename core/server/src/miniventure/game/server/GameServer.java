package miniventure.game.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameProtocol;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
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
						connectionToPlayerMap.put(connection, player);
						playerToConnectionMap.put(player, connection);
						Array<Chunk> playerChunks = level.getAreaChunks(player.getCenter(), 1, true, true);
						connection.sendTCP(new LevelData(level));
						Rectangle entityRect = null;
						for(Chunk chunk: playerChunks) {
							connection.sendTCP(new ChunkData(chunk, level));
							entityRect = entityRect == null ? chunk.getBounds() : entityRect.merge(chunk.getBounds());
						}
						connection.sendTCP(new SpawnData(new EntityAddition(player), player));
						for(Entity e: level.getOverlappingEntities(entityRect, player))
							connection.sendTCP(new EntityAddition(e));
						
						System.out.println("Server: new player successfully connected");
					}
					return;
				}
				
				ServerPlayer p = connectionToPlayerMap.get(connection);
				
				if(object instanceof ChunkRequest) {
					System.out.println("server received chunk request");
					ChunkRequest request = (ChunkRequest) object;
					ServerLevel level = p.getLevel(); // assumes player wants for current level
					if(level != null) {
						Chunk chunk = level.getChunk(request.x, request.y);
						connection.sendTCP(new ChunkData(chunk, level));
						Array<Entity> newEntities = level.getOverlappingEntities(chunk.getBounds());
						for(Entity e: newEntities)
							connection.sendTCP(new EntityAddition(e));
					} else
						System.err.println("Server could not satisfy chunk request, player level is null");
				}
				
				if(object instanceof PositionUpdate) {
					//System.out.println("server received movement");
					PositionUpdate m = (PositionUpdate) object;
					/*ServerLevel clientLevel;
					if(m.levelDepth == null) clientLevel = null;
					else clientLevel = world.getLevel(m.levelDepth);
					
					ServerLevel playerLevel = p.getLevel();
					if(!MyUtils.nullableEquals(clientLevel, playerLevel)) {
						// TO-DO the client thinks it's on the wrong level, for some reason. Send back a packet to fix it.
					}*/
					Vector3 newPos = new Vector3(m.x, m.y, m.z);
					p.move(newPos.cpy().sub(p.getLocation()));
					if(p.getLocation().dst(newPos) > 0.25f)
						connection.sendTCP(new PositionUpdate(p));
					//System.out.println("server has player at " + p.getPosition(true));
				}
				
				forPacket(object, StatUpdate.class, p::loadStat);
				
				if(object instanceof InteractRequest) {
					System.out.println("server received interaction request");
					InteractRequest r = (InteractRequest) object;
					if(r.playerPosition.variesFrom(p))
						connection.sendTCP(new PositionUpdate(p)); // fix the player's position
					if(r.attack) p.attack();
					else p.interact();
				}
				
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
		if(c != null) {
			if(obj instanceof EntityAddition)
				System.out.println("Server sending entity addition to 1 player.");
			
			c.sendTCP(obj);
		}
	}
	
	// levelMask is the level a player must be on to receive this data.
	public void broadcast(Object obj, Level levelMask, @NotNull ServerEntity... exclude) {
		if(levelMask == null) return; // no level, no packet.
		
		HashSet<ServerPlayer> players = new HashSet<>(playerToConnectionMap.keySet());
		HashSet<ServerEntity> excluded = new HashSet<>(Arrays.asList(exclude));
		//noinspection SuspiciousMethodCalls
		players.removeAll(excluded);
		players.removeIf(player -> !levelMask.equals(player.getLevel()));
		
		broadcast(obj, true, players);
	}
	public void broadcast(Object obj, @NotNull ServerEntity excludeEntity) {
		if(excludeEntity instanceof ServerPlayer)
			broadcast(obj, false, (ServerPlayer)excludeEntity);
		else
			broadcast(obj);
	}
	public void broadcast(Object obj, @NotNull ServerPlayer... excludedPlayers) { broadcast(obj, false, excludedPlayers); }
	public void broadcast(Object obj, boolean includeGiven, @NotNull ServerPlayer... players) { broadcast(obj, includeGiven, new HashSet<>(Arrays.asList(players))); }
	private void broadcast(Object obj, boolean includeGiven, @NotNull HashSet<ServerPlayer> players) {
		if(!includeGiven) {
			HashSet<ServerPlayer> playerSet = new HashSet<>(playerToConnectionMap.keySet());
			playerSet.removeAll(players);
			players = playerSet;
		}
		
		if(obj instanceof EntityAddition)
			System.out.println("Server sending entity addition to "+players.size()+" players.");
		
		for(ServerPlayer p: players)
			playerToConnectionMap.get(p).sendTCP(obj);
	}
}
