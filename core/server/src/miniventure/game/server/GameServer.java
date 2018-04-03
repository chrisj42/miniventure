package miniventure.game.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.ChatMessage;
import miniventure.game.chat.ChatMessageBuilder;
import miniventure.game.chat.ChatMessageLine;
import miniventure.game.item.ItemStack;
import miniventure.game.item.Recipe;
import miniventure.game.item.Recipes;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.ServerPlayer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameServer implements GameProtocol {
	
	private class PlayerData {
		final Connection connection;
		final ServerPlayer player;
		final ChatMessageBuilder toClientOut, toClientErr;
		boolean op;
		
		PlayerData(Connection connection, ServerPlayer player) {
			this.connection = connection;
			this.player = player;
			
			toClientOut = new ChatMessageBuilder(text -> new ChatMessageLine(Color.WHITE, text));
			toClientErr = new ChatMessageBuilder(toClientOut, text -> new ChatMessageLine(Color.RED, text));
			
			op = connection.getRemoteAddressTCP().getAddress().isLoopbackAddress();
		}
	}
	
	private final HashMap<Connection, PlayerData> connectionToPlayerDataMap = new HashMap<>();
	private final HashMap<ServerPlayer, Connection> playerToConnectionMap = new HashMap<>();
	
	private Server server;
	
	public GameServer() {
		server = new Server(writeBufferSize*5, objectBufferSize) {
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
		
		addListener(/*new LagListener(lagMin, lagMax, */new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ServerWorld world = ServerCore.getWorld();
				
				if(object instanceof Login) {
					System.out.println("server received login");
					Login login = (Login) object;
					
					if(login.version.compareTo(GameCore.VERSION) != 0) {
						connection.sendTCP(new LoginFailure("Required version: "+GameCore.VERSION));
						return;
					}
					
					String name = login.username;
					
					for(ServerPlayer player: playerToConnectionMap.keySet()) {
						if(player.getName().equals(name)) {
							connection.sendTCP(new LoginFailure("Username already exists."));
							return;
						}
					}
					
					// prepare level
					ServerLevel level = world.getLevel(0);
					if(level != null) {
						ServerPlayer player = world.addPlayer(name);
						connectionToPlayerDataMap.put(connection, new PlayerData(connection, player));
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
						
						System.out.println("Server: new player successfully connected: "+player.getName());
					}
					return;
				}
				
				PlayerData clientData = connectionToPlayerDataMap.get(connection);
				ServerPlayer client = clientData.player;
				
				if(object instanceof ChunkRequest) {
					//System.out.println("server received chunk request");
					ChunkRequest request = (ChunkRequest) object;
					ServerLevel level = client.getLevel(); // assumes player wants for current level
					if(level != null) {
						Chunk chunk = level.getChunk(request.x, request.y);
						//System.out.println("server sending back chunk "+chunk);
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
				
				forPacket(object, StatUpdate.class, client::loadStat);
				
				forPacket(object, MovementRequest.class, move -> {
					//Level lvl = world.getLevel(0);
					//System.out.println("server received movement request, start="+ Boundable.toLevelCoords(lvl, move.startPos.getPos())+", move="+move.getMoveDist()+", end="+Boundable.toLevelCoords(lvl, move.endPos.getPos()));
					Vector3 loc = client.getLocation();
					//System.out.println("current player location in server: "+Boundable.toLevelCoords(lvl, loc));
					if(move.getMoveDist().len() > 1 || move.startPos.variesFrom(client)) {
						//System.out.println("position varies, ignoring move and correcting player.");
						//connection.sendTCP(new PositionUpdate(p));
					} else {
						// move to start pos
						Vector3 start = move.startPos.getPos();
						//System.out.println("current player location as given by client: "+Boundable.toLevelCoords(lvl, start));
						Vector3 diff = loc.cpy().sub(start);
						//System.out.println("server location minus client location (will move this dist to start): "+diff);
						client.move(diff);
						//System.out.println("after initial movement to start pos, server player location: "+p.getLocation(true));
					}
					// move given dist
					Vector3 moveDist = move.getMoveDist();
					//System.out.println("server player will move dist: "+moveDist);
					client.move(moveDist);
					//System.out.println("server player position after main move: "+p.getLocation(true));
					// compare against given end pos
					if(move.endPos.variesFrom(client)) {
						//System.out.println("server is updating client");
						connection.sendTCP(new PositionUpdate(client));
					}
					//else if(!move.endPos.getPos().equals(p.getLocation()))
					//	p.moveTo(move.endPos.getPos());
					// TODO gonna have to take level into account above.
					// note that the server will always have the say when it comes to which level the player should be on.
				});
				
				if(object instanceof InteractRequest) {
					InteractRequest r = (InteractRequest) object;
					//System.out.println("server received interaction request; dir="+r.dir+", pos="+r.playerPosition.toString(world));
					if(r.playerPosition.variesFrom(client))
						connection.sendTCP(new PositionUpdate(client)); // fix the player's position
					
					client.setDirection(r.dir);
					if(r.attack) client.attack();
					else client.interact();
				}
				
				forPacket(object, ItemDropRequest.class, drop -> {
					ItemStack stack = ItemStack.load(drop.stackData);
					int removed = client.getInventory().removeItem(stack);
					ServerLevel level = client.getLevel();
					if(level == null) return;
					// get target pos, which is one tile in front of player.
					Vector2 targetPos = client.getCenter();
					targetPos.add(client.getDirection().getVector()); // adds 1 in the direction of the player.
					for(int i = 0; i < removed; i++)
						level.dropItem(stack.item.copy(), client.getPosition(), targetPos);
				});
				
				forPacket(object, HeldItemRequest.class, handItem -> {
					ItemStack stack = ItemStack.load(handItem.stackData);
					//System.out.println("server received held item request: "+stack);
					client.getHands().clearItem(client.getInventory());
					//System.out.println("server player inventory: "+Arrays.toString(p.getInventory().save()));
					int count = client.getInventory().removeItem(stack);
					if(count > 0)
						client.getHands().setItem(stack.item, count);
					connection.sendTCP(new InventoryUpdate(client));
				});
				
				forPacket(object, CraftRequest.class, req -> {
					Recipe recipe = Recipes.recipes[req.recipeIndex];
					recipe.tryCraft(client.getInventory());
					connection.sendTCP(new InventoryUpdate(client));
				});
				
				if(object.equals(DatalessRequest.Respawn)) {
					//ServerPlayer client = connectionToPlayerDataMap.get(connection).player;
					world.respawnPlayer(client);
					connection.sendTCP(new SpawnData(new EntityAddition(client), client));
				}
				
				forPacket(object, Message.class, msg -> {
					System.out.println("server: executing command "+msg.msg);
					ServerCore.getCommandInput().executeCommand(msg.msg, client, clientData.toClientOut, clientData.toClientErr);
					ChatMessage output = clientData.toClientOut.flushMessage();
					if(output != null)
						connection.sendTCP(output);
				});
			}
			
			/*@Override
			public void connected(Connection connection) {
				System.out.println("new connection: " + connection.getRemoteAddressTCP().getHostString());
			}*/
			
			@Override
			public void disconnected(Connection connection) {
				PlayerData data = connectionToPlayerDataMap.get(connection);
				if(data == null) return;
				ServerPlayer player = data.player;
				if(player != null) {
					player.remove();
					player.getWorld().removePlayer(player);
				}
				//System.out.println("server disconnected from client: " + connection.getRemoteAddressTCP().getHostString());
			}
		});//);
		
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
	
	@Nullable
	public ServerPlayer getPlayerByName(String name) {
		for(ServerPlayer player: playerToConnectionMap.keySet())
			if(player.getName().equalsIgnoreCase(name))
				return player;
		
		return null;
	}
	
	public boolean isAdmin(@Nullable ServerPlayer player) {
		if(player == null) return true; // from server command prompt
		PlayerData data = connectionToPlayerDataMap.get(playerToConnectionMap.get(player));
		if(data == null) return false; // unrecognized player (should never happen)
		return data.op;
	}
	
	void stop() { server.stop(); }
}
