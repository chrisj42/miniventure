package miniventure.game.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameProtocol;
import miniventure.game.item.ItemStack;
import miniventure.game.item.Recipe;
import miniventure.game.item.Recipes;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.LagListener;
import com.esotericsoftware.kryonet.Server;

import org.jetbrains.annotations.NotNull;

public class GameServer implements GameProtocol {
	
	private final HashMap<Connection, ServerPlayer> connectionToPlayerMap = new HashMap<>();
	private final HashMap<ServerPlayer, Connection> playerToConnectionMap = new HashMap<>();
	
	private Server server;
	
	public GameServer() {
		server = new Server(writeBufferSize, objectBufferSize) {
			@Override
			public void start() {
				Thread thread = new Thread(this, "Server");
				thread.setUncaughtExceptionHandler((t, ex) -> {
					t.getThreadGroup().uncaughtException(t, ex);
					close();
				});
				thread.start();
			}
		};
		GameProtocol.registerClasses(server.getKryo());
		
		addListener(new LagListener(lagMin, lagMax, new Listener() {
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
						System.out.println("server sending back chunk "+chunk);
						connection.sendTCP(new ChunkData(chunk, level));
						Array<Entity> newEntities = level.getOverlappingEntities(chunk.getBounds());
						for(Entity e: newEntities)
							connection.sendTCP(new EntityAddition(e));
					} else
						System.err.println("Server could not satisfy chunk request, player level is null");
				}
				
				forPacket(object, EntityRequest.class, req -> {
					Entity e = world.getEntity(req.eid);
					if(e != null)
						connection.sendTCP(new EntityAddition(e));
				});
				
				forPacket(object, StatUpdate.class, p::loadStat);
				
				forPacket(object, MovementRequest.class, move -> {
					if(move.getMoveDist().len() > 1) {
						connection.sendTCP(new PositionUpdate(p));
						return;
					}
					// move to start pos
					p.move(p.getLocation().sub(move.startPos.getPos()));
					// move given dist
					p.move(move.getMoveDist());
					// compare against given end pos
					if(move.endPos.variesFrom(p))
						connection.sendTCP(new PositionUpdate(p));
					else if(!move.endPos.getPos().equals(p.getLocation()))
						p.moveTo(move.endPos.getPos());
					// TODO gonna have to take level into account above.
					// note that the server will always have the say when it comes to which level the player should be on.
				});
				
				if(object instanceof InteractRequest) {
					InteractRequest r = (InteractRequest) object;
					//System.out.println("server received interaction request; dir="+r.dir+", pos="+r.playerPosition.toString(world));
					if(r.playerPosition.variesFrom(p))
						connection.sendTCP(new PositionUpdate(p)); // fix the player's position
					
					p.setDirection(r.dir);
					if(r.attack) p.attack();
					else p.interact();
				}
				
				forPacket(object, ItemDropRequest.class, drop -> {
					ItemStack stack = ItemStack.load(drop.stackData);
					int removed = p.getInventory().removeItem(stack);
					ServerLevel level = p.getLevel();
					if(level == null) return;
					// get target pos, which is one tile in front of player.
					Vector2 targetPos = p.getCenter();
					targetPos.add(p.getDirection().getVector()); // adds 1 in the direction of the player.
					for(int i = 0; i < removed; i++)
						level.dropItem(stack.item.copy(), p.getPosition(), targetPos);
				});
				
				forPacket(object, HeldItemRequest.class, handItem -> {
					ItemStack stack = ItemStack.load(handItem.stackData);
					//System.out.println("server received held item request: "+stack);
					p.getHands().clearItem(p.getInventory());
					//System.out.println("server player inventory: "+Arrays.toString(p.getInventory().save()));
					int count = p.getInventory().removeItem(stack);
					if(count > 0)
						p.getHands().setItem(stack.item, count);
					connection.sendTCP(new InventoryUpdate(p));
				});
				
				forPacket(object, CraftRequest.class, req -> {
					Recipe recipe = Recipes.recipes[req.recipeIndex];
					recipe.tryCraft(p.getInventory());
					connection.sendTCP(new InventoryUpdate(p));
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
		}));
		
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
		
		for(ServerPlayer p: players)
			playerToConnectionMap.get(p).sendTCP(obj);
	}
	
	void stop() { server.stop(); }
}
