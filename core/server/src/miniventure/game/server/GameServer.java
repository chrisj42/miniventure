package miniventure.game.server;

import javax.swing.Timer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageBuilder;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Command;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.item.Inventory;
import miniventure.game.item.Recipe;
import miniventure.game.item.Recipes;
import miniventure.game.item.ServerItem;
import miniventure.game.item.ServerItemStack;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.ServerHands;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.entity.particle.ParticleData;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.MiniventureServer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameServer implements GameProtocol {
	
	private static final Color SERVER_CHAT_COLOR = Color.WHITE;
	private static final Color STATUS_MSG_COLOR = Color.ORANGE;
	private static final Color ERROR_CHAT_COLOR = Color.RED;
	
	private class PlayerData {
		final Connection connection;
		@NotNull final ServerPlayer player;
		final InfoMessageBuilder toClientOut, toClientErr;
		boolean op;
		final Timer validationTimer;
		
		/** @see GameProtocol.InventoryRequest */
		private boolean inventoryMode = false; // send hotbar updates at first
		
		PlayerData(Connection connection, @NotNull ServerPlayer player) {
			this.connection = connection;
			this.player = player;
			
			toClientOut = new InfoMessageBuilder(text -> new InfoMessageLine(GameCore.DEFAULT_CHAT_COLOR, text));
			toClientErr = new InfoMessageBuilder(toClientOut, text -> new InfoMessageLine(ERROR_CHAT_COLOR, text));
			
			op = connection.getRemoteAddressTCP().getAddress().isLoopbackAddress();
			
			validationTimer = new Timer(5000, e -> sendEntityValidation(this));
			validationTimer.setRepeats(true);
		}
	}
	
	private final HashMap<Connection, ServerThread> connectionThreadMap = new HashMap<>();
	
	private final Map<Connection, PlayerData> connectionToPlayerDataMap = new HashMap<>();
	private final Map<ServerPlayer, Connection> playerToConnectionMap = new HashMap<>();
	private final Object playerLock = new Object();
	
	@NotNull private final ServerWorld world;
	private InetSocketAddress host;
	private final int port;
	private MiniventureServer server;
	private boolean multiplayer;
	
	public GameServer(@NotNull ServerWorld world, int port, boolean multiplayer) throws IOException {
		this.world = world;
		this.port = port;
		this.multiplayer = multiplayer;
		server = new MiniventureServer(writeBufferSize*5, objectBufferSize);
		GameProtocol.registerClasses(server.getKryo());
		
		server.addListener(actor);
		server.start();
		server.bind(port);
	}
	
	public int getPort() { return port; }
	
	public boolean isMultiplayer() { return multiplayer; }
	public void setMultiplayer(boolean multiplayer) {
		this.multiplayer = multiplayer;
	}
	
	public void setHost(InetSocketAddress host) { this.host = host; }
	private boolean isHost(@NotNull InetSocketAddress host) { return host.equals(this.host); }
	
	public boolean isHost(@NotNull ServerPlayer player) {
		InetSocketAddress addr = getPlayerAddress(player);
		return addr != null && isHost(addr);
	}
	
	
	private PlayerData getPlayerData(@Nullable ServerPlayer player) {
		if(player == null)
			return null;
		synchronized (playerLock) {
			return connectionToPlayerDataMap.get(playerToConnectionMap.get(player));
		}
	}
	
	private PlayerData getPlayerData(@NotNull Connection connection) {
		synchronized (playerLock) {
			return connectionToPlayerDataMap.get(connection);
		}
	}
	
	private Connection getConnection(@Nullable ServerPlayer player) {
		synchronized (playerLock) {
			return playerToConnectionMap.get(player);
		}
	}
	
	
	<T> void forPacket(Object packet, Class<T> type, boolean sync, ValueFunction<T> response) {
		if(sync)
			world.postRunnable(() -> GameProtocol.super.forPacket(packet, type, response));
		else
			GameProtocol.super.forPacket(packet, type, response);
	}
	
	// this will only ever be called in one thread: the server thread. So synchronizing the connectionThreadMap isn't necessary.
	private final Listener actor = new Listener() {
		@Override
		public void connected(Connection connection) {
			// prevent further connections unless desired.
			// System.out.println("attempted connection from address "+connection.getRemoteAddressTCP());
			if(!multiplayer && connectionThreadMap.size() > 0 && !isHost(connection.getRemoteAddressTCP())) {
				connection.sendTCP(new LoginFailure("World is private."));
				connection.close();
			}
			else {
				ServerThread st = new ServerThread(connection, packetHandler);
				connectionThreadMap.put(connection, st);
				st.start();
			}
		}
		
		@Override
		public void received (Connection connection, Object object) {
			if(object instanceof KeepAlive)
				return; // we don't care about these, they are internal packets
			
			ServerThread st = connectionThreadMap.get(connection);
			if(st != null)
				st.addPacket(object);
			else
				GameCore.error("recieved packet from unregistered connection: "+connection.getRemoteAddressTCP());
		}
		
		@Override
		public void disconnected(Connection connection) {
			ServerThread st = connectionThreadMap.remove(connection);
			if(st == null) return;
			st.end();
			
			PlayerData data = getPlayerData(connection);
			if(data == null) return;
			data.validationTimer.stop();
			ServerPlayer player = data.player;
			world.postRunnable(() -> {
				world.savePlayer(player);
				player.remove();
				// player.getWorld().removePlayer(player);
				synchronized (playerLock) {
					connectionToPlayerDataMap.remove(connection);
					playerToConnectionMap.remove(player);
				}
				broadcast(new Message(player.getName()+" left the server.", STATUS_MSG_COLOR));
				//System.out.println("server disconnected from client: " + connection.getRemoteAddressTCP().getHostString());
				
				if(player.getName().equals(HOST)) // quit server when host exits
					world.exitWorld();
			});
		}
	};
	
	private final PacketHandler packetHandler = (connection, object) -> {
		final ServerWorld world = GameServer.this.world;
		
		if(object instanceof Login) {
			GameCore.debug("server received login");
			Login login = (Login) object;
			
			if(login.version.compareTo(GameCore.VERSION) != 0) {
				connection.sendTCP(new LoginFailure("Required version: "+GameCore.VERSION));
				return;
			}
			
			String name = login.username;
			
			if(name.equals(HOST) && !isHost(connection.getRemoteAddressTCP())) {
				connection.sendTCP(new LoginFailure("Username '"+HOST+"' reserved for server host."));
				return;
			}
			
			// TODO implement passwords
			/*if(!world.checkPassword(name, "")) {
				GameCore.debug("Server rejecting a login request with username '"+name+"' due to incorrect password.");
				connection.sendTCP(new LoginFailure("Password is incorrect."));
				return;
			}*/
			
			ServerPlayer player;
			PlayerData playerData;
			synchronized (playerLock) {
				for(ServerPlayer p: playerToConnectionMap.keySet()) {
					if(p.getName().equals(name)) {
						connection.sendTCP(new LoginFailure("A player named '"+name+"' is already logged in."));
						return;
					}
				}
				
				player = world.addPlayer(name);
				
				playerData = new PlayerData(connection, player);
				connectionToPlayerDataMap.put(connection, playerData);
				playerToConnectionMap.put(player, connection);
			}
			
			connection.sendTCP(world.getWorldUpdate());
			world.postRunnable(() -> {
				world.loadPlayer(player);
				
				System.out.println("Server: new player successfully connected: "+player.getName());
				
				playerData.validationTimer.start();
				
				broadcast(new Message(player.getName()+" joined the server.", STATUS_MSG_COLOR), false, player);
			});
			
			return;
		}
		
		PlayerData clientData = getPlayerData(connection);
		if(clientData == null) {
			System.err.println("server received packet from unknown client "+connection.getRemoteAddressTCP().getHostString()+"; ignoring packet "+object);
			return;
		}
		
		ServerPlayer client = clientData.player;
		
		forPacket(object, Ping.class, ping -> {
			long nano = System.nanoTime() - ping.start;
			double time = nano / 1E9D;
			
			String unit;
			if(time < 1) {
				time *= 1E3;
				unit = "ms";
			} else
				unit = "seconds";
			
			int whole = (int) time;
			int decimal = (int) (time * 1E3) - whole; // three decimals
			
			String disp = "Ping for '"+client.getName()+"': "+whole+'.'+decimal+' '+unit+'.';
			if(ping.source == null) // server origin
				System.out.println(disp);
			else {
				Connection c;
				if(ping.source.equals(client.getName()))
					c = connection;
				else {
					synchronized (playerLock) {
						c = playerToConnectionMap.get(getPlayerByName(ping.source));
					}
				}
				
				if(c != null)
					c.sendTCP(new Message(disp, Color.SKY));
			}
		});
		
		forPacket(object, MapRequest.class, true, req -> connection.sendTCP(world.getMapData()));
		
		forPacket(object, LevelChange.class, true, change -> {
			// check to see if the level exists (client shouldn't be able to crash server), and if the client is allowed to change to the given level
			// if the move is invalid, then send a simple spawn packet; the client doesn't unload a level unless a new LevelData packet is recieved.
			
			Level clientLevel = client.getLevel();
			if(clientLevel != null && change.levelid == clientLevel.getLevelId())
				return; // quietly ignore level requests for the same level
			
			// todo check if move is valid in terms of stats (prob using class/method in common module)
			// for now we will assume it is.
			
			world.despawnPlayer(client);
			world.loadLevel(change.levelid, client, level -> {
				Tile spawnTile = level.getMatchingTiles(TileTypeEnum.DOCK).get(0);
				client.moveTo(spawnTile);
			});
			
			// broadcast(new EntityUpdate(client.getTag(), new PositionUpdate(client), null));
		});
				
				/*if(object instanceof ChunkRequest) {
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
				}*/
		
		forPacket(object, EntityRequest.class, true, req -> {
			Entity e = world.getEntity(req.eid);
			if(e != null && e.getLevel() == client.getLevel())
				connection.sendTCP(new EntityAddition(e));
		});
		
		// TODO don't allow client to update server stats
		forPacket(object, StatUpdate.class, true, client::loadStat);
		
		forPacket(object, MovementRequest.class, true, move -> {
				/*Vector3 loc = client.getLocation();
				if(move.getMoveDist().len() < 1 && !move.startPos.variesFrom(client)) {
					// move to start pos
					Vector3 start = move.startPos.getPos();
					Vector3 diff = loc.cpy().sub(start);
					client.move(diff);
				}*/
			// move given dist
			Vector3 moveDist = move.getMoveDist();
			if(!GameCore.debug) // TODO replace this static speed check with something that determines the player's speed with respect to their situation.
				moveDist.clamp(0, Math.min(.5f, 2.5f*Player.MOVE_SPEED/Math.min(world.getFPS(), 60))); // the server will not allow the client to move fast (unless in debug mode)
			client.move(moveDist);
			// compare against given end pos
			if(move.endPos.variesFrom(client))
				connection.sendTCP(new PositionUpdate(client));
			else
				client.moveTo(move.endPos.getPos());
			// note that the server will always have the say when it comes to which level the player should be on.
		});
		
		forPacket(object, InteractRequest.class, true, r -> {
			// if(r.playerPosition.variesFrom(client))
			// 	connection.sendTCP(new PositionUpdate(client)); // fix the player's position
			
			client.doInteract(r.dir, r.hotbarIndex, r.attack);
		});
		
		forPacket(object, ItemDropRequest.class, true, drop -> {
			ServerHands hands = client.getHands();
			Inventory inv = client.getInventory();
			ServerItem item;
			
			if(clientData.inventoryMode)
				// dropped from the inventory screen
				item = inv.getItem(drop.index);
			else
				// dropped from the hotbar
				item = hands.getItem(drop.index);
			
			if(item == null) {
				// no item; client must have made a mistake (it shouldn't be sending a request if there isn't an item); if it came from their hotbar, update it, but in inventory mode it would get too messy so just ignore it; we'll update the inv once they close the menu in that case.
				if(!clientData.inventoryMode)
					connection.sendTCP(client.getHotbarUpdate());
				return;
			}
			
			ServerItemStack drops;
			if(drop.all)
				drops = new ServerItemStack(item, client.getInventory().removeItemStack(item));
			else
				drops = new ServerItemStack(item, client.getInventory().removeItem(item) ? 1 : 0);
			
			if(!clientData.inventoryMode) {
				hands.validate();
				connection.sendTCP(client.getHotbarUpdate());
			}
			// inventory screen mode: don't send back item removals, the client should manage by itself. Only send back additions.
			
			ServerLevel level = client.getLevel();
			if(level == null) return;
			// get target pos, which is one tile in front of player.
			Vector2 targetPos = client.getCenter();
			targetPos.add(client.getDirection().getVector().scl(2)); // adds 2 in the direction of the player.
			for(int i = 0; i < drops.count; i++)
				level.dropItem(drops.item, true, client.getCenter(), targetPos);
		});
		
		forPacket(object, InventoryRequest.class, true, req -> {
			clientData.inventoryMode = req.hotbar == null;
			if(req.hotbar != null) {
				client.getHands().fromInventoryIndex(req.hotbar);
				connection.sendTCP(client.getHotbarUpdate());
			}
			else
				connection.sendTCP(client.getInventoryUpdate());
		});
		
		forPacket(object, RecipeRequest.class, true, req ->
			connection.sendTCP(new RecipeRequest(
				Recipes.serializeRecipes(),
				new RecipeStockUpdate(client.getInventory().getItemStacks())
			))
		);
		
		forPacket(object, CraftRequest.class, true, req -> {
			Recipe recipe = Recipes.recipes[req.recipeIndex];
			ServerItem[] left = recipe.tryCraft(client.getInventory());
			if(left != null) {
				ServerLevel level = client.getLevel();
				if(level != null)
					for(ServerItem item : left)
						level.dropItem(item, client.getPosition(), null);
			}
			client.getHands().validate();
			connection.sendTCP(client.getHotbarUpdate());
			connection.sendTCP(new RecipeStockUpdate(client.getInventory().getItemStacks()));
		});
		
		if(object.equals(DatalessRequest.Respawn)) {
			world.postRunnable(() -> {
				world.respawnPlayer(client);
				connection.sendTCP(new SpawnData(new EntityAddition(client), client.getHotbarUpdate(), client.saveStats()));
			});
		}
		
		if(object.equals(DatalessRequest.Tile)) {
			Level level = client.getLevel();
			if(level != null) {
				Tile t = level.getTile(client.getInteractionRect());
				connection.sendTCP(new Message(client+" looking at "+(t==null?null:t.toLocString()), GameCore.DEFAULT_CHAT_COLOR));
			}
		}
		
		forPacket(object, Message.class, true, msg -> {
			//System.out.println("server: executing command "+msg.msg);
			CommandInputParser.executeCommand(world, msg.msg, client, clientData.toClientOut, clientData.toClientErr);
			InfoMessage output = clientData.toClientOut.flushMessage();
			if(output != null)
				connection.sendTCP(output);
		});
		
		forPacket(object, TabRequest.class, request -> {
			String text = request.manualText;
			if(text.split(" ").length == 1) {
				// hasn't finished entering command name, autocomplete that
				Array<String> matches = new Array<>(String.class);
				for(Command c: Command.valuesFor(client)) {
					String name = c.name();
					if(text.length() == 0 || name.toLowerCase().contains(text.toLowerCase()))
						matches.add(name);
				}
				
				if(matches.size == 0) return;
				
				if(matches.size == 1) {
					connection.sendTCP(new TabResponse(request.manualText, matches.get(0)));
					return;
				}
				
				matches.sort(String::compareToIgnoreCase);
				
				if(request.tabIndex < 0)
					connection.sendTCP(new Message(ArrayUtils.arrayToString(matches.shrink(), ", "), GameCore.DEFAULT_CHAT_COLOR));
				else {
					// actually autocomplete
					connection.sendTCP(new TabResponse(request.manualText, matches.get(request.tabIndex % matches.size)));
				}
			}
		});
		
		forPacket(object, SelfHurt.class, true, hurt -> {
			// assumed that the client has hurt itself in some way
			client.attackedBy(client, null, hurt.dmg);
		});
	};
	
	public Set<ServerPlayer> getPlayers() {
		synchronized (playerToConnectionMap) {
			return new HashSet<>(playerToConnectionMap.keySet());
		}
	}
	
	public void sendToPlayer(@NotNull ServerPlayer player, Object obj) {
		Connection c = getConnection(player);
		if(c != null)
			c.sendTCP(obj);
	}
	
	// levelMask is the level a player must be on to receive this data.
	public void broadcast(Object obj, Level levelMask, @NotNull ServerEntity... exclude) {
		if(levelMask == null) return; // no level, no packet.
		
		Set<ServerPlayer> players = getPlayers();
		HashSet<ServerEntity> excluded = new HashSet<>(Arrays.asList(exclude));
		//noinspection SuspiciousMethodCalls
		players.removeAll(excluded);
		players.removeIf(player -> !levelMask.equals(player.getLevel()));
		
		broadcast(obj, true, players);
	}
	public void broadcast(Object obj, @NotNull ServerEntity excludeIfPlayer) {
		if(excludeIfPlayer instanceof ServerPlayer)
			broadcast(obj, false, (ServerPlayer)excludeIfPlayer);
		else
			broadcast(obj);
	}
	public void broadcast(Object obj, @NotNull ServerPlayer... excludedPlayers) { broadcast(obj, false, excludedPlayers); }
	public void broadcast(Object obj, boolean includeGiven, @NotNull ServerPlayer... players) { broadcast(obj, includeGiven, new HashSet<>(Arrays.asList(players))); }
	private void broadcast(Object obj, boolean includeGiven, @NotNull Set<ServerPlayer> players) {
		if(!includeGiven) {
			Set<ServerPlayer> playerSet = getPlayers();
			playerSet.removeAll(players);
			players = playerSet;
		}
		
		for(ServerPlayer p: players)
			sendToPlayer(p, obj);
	}
	
	@Nullable
	public ServerPlayer getPlayerByName(String name) {
		for(ServerPlayer player: getPlayers())
			if(player.getName().equalsIgnoreCase(name))
				return player;
		
		return null;
	}
	
	public InetSocketAddress getPlayerAddress(@NotNull ServerPlayer player) {
		Connection c = getConnection(player);
		if(c != null)
			return c.getRemoteAddressTCP();
		return null;
	}
	
	public void sendLevel(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		Connection connection = getConnection(player);
		if(connection == null) {
			System.err.println("Could not send level to player "+player+", Connection obj could not be found.");
			return;
		}
		
		world.postRunnable(() -> {
			connection.sendTCP(new LevelData(level));
			connection.sendTCP(new SpawnData(new EntityAddition(player), player.getHotbarUpdate(), player.saveStats()));
			for(Entity e: level.getEntities())
				connection.sendTCP(new EntityAddition(e));
		});
	}
	
	public boolean isInventoryMode(@NotNull ServerPlayer player) {
		PlayerData data = getPlayerData(player);
		return data != null && data.inventoryMode;
	}
	
	public boolean isAdmin(@Nullable ServerPlayer player) {
		if(player == null) return true; // from server command prompt
		PlayerData data = getPlayerData(player);
		if(data == null) return false; // unrecognized player (should never happen)
		return data.op;
	}
	
	public boolean setAdmin(@NotNull ServerPlayer player, boolean op) {
		PlayerData data = getPlayerData(player);
		if(data == null) return false; // unrecognized player (should never happen)
		data.op = op;
		return true;
	}
	
	public void sendMessage(@Nullable ServerPlayer sender, ServerPlayer reciever, String msg) {
		sendToPlayer(reciever, getMessage(sender, msg));
	}
	public void broadcastMessage(@Nullable ServerPlayer sender, String msg) {
		broadcast(getMessage(sender, msg));
	}
	
	public Message getMessage(@Nullable ServerPlayer sender, String msg) {
		if(sender == null) return new Message("Server: "+msg, SERVER_CHAT_COLOR);
		return new Message((multiplayer?sender.getName()+": ":"")+msg, GameCore.DEFAULT_CHAT_COLOR);
	}
	
	public void broadcastParticle(ParticleData data, WorldObject posMarker) {
		broadcastParticle(data, posMarker.getLevel(), posMarker.getCenter());
	}
	public void broadcastParticle(ParticleData data, Level level, Vector2 pos) {
		broadcast(new ParticleAddition(data, new PositionUpdate(level, pos)));
	}
	
	private void sendEntityValidation(@NotNull PlayerData pData) {
		ServerPlayer player = pData.player;
		if(!pData.connection.isConnected()) {
			System.err.println("Server not sending entity validation to player "+player.getName()+" because player has disconnected; stopping validation timer.");
			pData.validationTimer.stop();
			return;
		}
		
		ServerLevel level = player.getLevel();
		if(level == null) {
			GameCore.debug("Server: Level of player "+player.getName()+" is null during entity validation attempt, skipping validation.");
			return;
		}
		
		world.postRunnable(() -> {
			EntityValidation validation = new EntityValidation(level, player);
			pData.connection.sendTCP(validation);
		});
	}
	
	public void playEntitySound(String soundName, Entity source) { playEntitySound(soundName, source, true); }
	public void playEntitySound(String soundName, Entity source, boolean broadcast) {
		if(source instanceof Player)
			playGenericSound("player/"+soundName, source.getCenter());
		//else if(source instanceof MobAi)
		//	playSound("entity/"+((MobAi)source).getType().name().toLowerCase()+"/");
		else
			playGenericSound("entity/"+soundName, source.getCenter());
	}
	public void playTileSound(String soundName, Tile tile, TileTypeEnum type) {
		//playGenericSound("tile/"+type+"/"+soundName, tile.getCenter());
		playGenericSound("tile/"+soundName, tile.getCenter());
	}
	public void playGenericSound(String soundName, Vector2 source) {
		for(ServerPlayer player: getPlayers()) {
			if(player.getPosition().dst(source) <= GameCore.SOUND_RADIUS) {
				Connection c = getConnection(player);
				if(c != null)
					c.sendTCP(new SoundRequest(soundName));
			}
		}
	}
	
	public void printStatus(MessageBuilder out) {
		out.println("Miniventure Version: "+GameCore.VERSION);
		out.println("Server Running: "+(server.getUpdateThread() != null && server.getUpdateThread().isAlive()));
		out.println("FPS: " + world.getFPS());
		out.println("Players connected: "+playerToConnectionMap.size());
		Collection<PlayerData> data;
		synchronized (playerLock) {
			data = new ArrayList<>(connectionToPlayerDataMap.values());
		}
		for(PlayerData pd: data) {
			out.print("     Player '"+pd.player.getName()+"' ");
			if(pd.op) out.print("(admin) ");
			out.print(pd.player.getLocation(true));
			out.println();
		}
		if(GameCore.debug)
			out.println("Debug mode is enabled.");
	}
	
	public void stop(boolean wait) {
		server.stop();
		while(wait) {
			MyUtils.sleep(25);
			wait = playerToConnectionMap.size() > 0;
		}
	}
}
