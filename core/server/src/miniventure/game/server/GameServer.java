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
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.ServerEntity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.entity.particle.ParticleData;
import miniventure.game.world.file.PlayerData;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.MiniventureServer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameServer implements GameProtocol {
	
	public static final Color SERVER_CHAT_COLOR = Color.WHITE;
	public static final Color STATUS_MSG_COLOR = Color.ORANGE;
	public static final Color ERROR_CHAT_COLOR = new Color(1f, .5f, .5f, 1);
	
	// public static final String PLAYER_NAME_REGEX = "( *[a-zA-Z0-9_.-] *)+";
	
	private class PlayerLink {
		final Connection connection;
		@NotNull final ServerPlayer player;
		final InfoMessageBuilder toClientOut, toClientErr;
		final Timer validationTimer;
		
		boolean op;
		
		/** @see GameProtocol.InventoryRequest */
		// private boolean inventoryMode = false; // send hotbar updates at first
		
		PlayerLink(Connection connection, @NotNull ServerPlayer player, boolean op) {
			this.connection = connection;
			this.player = player;
			this.op = op;
			
			toClientOut = new InfoMessageBuilder(text -> new InfoMessageLine(GameCore.DEFAULT_CHAT_COLOR, text));
			toClientErr = new InfoMessageBuilder(toClientOut, text -> new InfoMessageLine(ERROR_CHAT_COLOR, text));
			
			validationTimer = new Timer(5000, e -> sendEntityValidation(this));
			validationTimer.setRepeats(true);
		}
	}
	
	// private final HashMap<Connection, ServerThread> connectionThreadMap = new HashMap<>();
	private final Map<String, PlayerData> knownPlayers = Collections.synchronizedMap(new HashMap<>());
	private final Object dataLock = new Object();
	
	private final Map<Connection, PlayerLink> connectionToPlayerInfoMap = new HashMap<>();
	private final Map<ServerPlayer, Connection> playerToConnectionMap = new HashMap<>();
	private final Object playerLock = new Object();
	
	@NotNull private final ServerWorld world;
	// private InetSocketAddress host;
	private final int port;
	private MiniventureServer server;
	private boolean multiplayer;
	private final MapFunction<InetSocketAddress, Boolean> connectionValidator;
	
	public GameServer(@NotNull ServerWorld world, int port, boolean multiplayer, MapFunction<InetSocketAddress, Boolean> connectionValidator, PlayerData[] playerData) throws IOException {
		this.world = world;
		this.port = port;
		this.multiplayer = multiplayer;
		this.connectionValidator = connectionValidator;
		server = new MiniventureServer(writeBufferSize*5, objectBufferSize);
		GameProtocol.registerClasses(server.getKryo());
		
		for(PlayerData info: playerData)
			knownPlayers.put(info.name, info);
		
		server.addListener(actor);
		server.start();
		server.bind(port);
	}
	
	public int getPort() { return port; }
	
	public boolean isMultiplayer() { return multiplayer; }
	public void setMultiplayer(boolean multiplayer) {
		this.multiplayer = multiplayer;
	}
	
	// public void setHost(InetSocketAddress host) { this.host = host; }
	private boolean isHost(@NotNull InetSocketAddress host) { return connectionValidator.get(host); }
	
	public boolean isHost(@NotNull ServerPlayer player) {
		InetSocketAddress addr = getPlayerAddress(player);
		return addr != null && isHost(addr);
	}
	
	
	public PlayerData[] updatePlayerData() {
		ServerPlayer[] players;
		synchronized (playerLock) {
			players = playerToConnectionMap.keySet().toArray(new ServerPlayer[0]);
		}
		for(ServerPlayer p: players)
			updatePlayerData(p);
		
		synchronized (dataLock) {
			return knownPlayers.values().toArray(new PlayerData[0]);
		}
	}
	public void updatePlayerData(ServerPlayer player) {
		PlayerLink info = getPlayerInfo(player);
		if(info == null) {
			System.err.println("Player "+player+" does not have PlayerLink, cannot update data.");
			return;
		}
		PlayerData pdata;
		synchronized (dataLock) {
			pdata = knownPlayers.get(player.getName());
		}
		if(pdata == null) {
			System.err.println("Player "+player+" does not have PlayerData, cannot update.");
			return;
		}
		
		world.postRunnable(false, () -> {
			final String name = player.getName();
			final String data = player.serialize();
			final Level level = player.getLevel();
			// using the pdata level id is better than using the spawn level because it doesn't lose the info if an extra update request is sent after the player is removed from their level.
			final int levelid = level == null ? pdata.levelId : level.getLevelId();
			synchronized (dataLock) {
				knownPlayers.put(name, new PlayerData(name, pdata.passhash, data, levelid, info.op));
			}
		});
	}
	
	private PlayerLink getPlayerInfo(@Nullable ServerPlayer player) {
		if(player == null)
			return null;
		synchronized (playerLock) {
			return connectionToPlayerInfoMap.get(playerToConnectionMap.get(player));
		}
	}
	
	private PlayerLink getPlayerInfo(@NotNull Connection connection) {
		synchronized (playerLock) {
			return connectionToPlayerInfoMap.get(connection);
		}
	}
	
	private Connection getConnection(@Nullable ServerPlayer player) {
		synchronized (playerLock) {
			return playerToConnectionMap.get(player);
		}
	}
	
	
	private <T> void forPacket(Object packet, Class<T> type, boolean sync, ValueAction<T> response) {
		if(sync)
			world.postRunnable(() -> GameProtocol.forPacket(packet, type, response));
		else
			GameProtocol.forPacket(packet, type, response);
	}
	
	// this will only ever be called in one thread: the server thread. So synchronizing the connectionThreadMap isn't necessary.
	private final Listener actor = new Listener() {
		@Override
		public void connected(Connection connection) {
			// prevent further connections unless desired.
			// System.out.println("attempted connection from address "+connection.getRemoteAddressTCP());
			// System.out.println("server: comparing given "+connection.getRemoteAddressTCP()+" to host "+host);
			if(!multiplayer && /*connectionThreadMap.size() > 0 &&*/ !isHost(connection.getRemoteAddressTCP())) {
				connection.sendTCP(new LoginFailure("World is private."));
				connection.close();
			}
			/*else {
				ServerThread st = new ServerThread(connection, packetHandler);
				connectionThreadMap.put(connection, st);
				st.start();
			}*/
		}
		
		@Override
		public void received (Connection connection, Object object) {
			if(object instanceof KeepAlive)
				return; // we don't care about these, they are internal packets
			
			// ServerThread st = connectionThreadMap.get(connection);
			// if(st != null)
			// 	st.addPacket(object);
			// else
			// 	GameCore.error("recieved packet from unregistered connection: "+connection.getRemoteAddressTCP());
			packetHandler.handle(connection, object);
		}
		
		@Override
		public void disconnected(Connection connection) {
			// ServerThread st = connectionThreadMap.remove(connection);
			// if(st == null) return;
			// st.end();
			
			PlayerLink info = getPlayerInfo(connection);
			if(info == null) return;
			info.validationTimer.stop();
			ServerPlayer player = info.player;
			world.postRunnable(() -> {
				updatePlayerData(player);
				player.remove();
				// player.getWorld().removePlayer(player);
				synchronized (playerLock) {
					connectionToPlayerInfoMap.remove(connection);
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
			final boolean isHost = isHost(connection.getRemoteAddressTCP());
			
			if(name.equals(HOST) && !isHost) {
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
			PlayerLink pinfo;
			// synchronized (playerLock) {
			for(ServerPlayer p: playerToConnectionMap.keySet()) {
				if(p.getName().equals(name)) {
					connection.sendTCP(new LoginFailure("A player named '"+name+"' is already logged in."));
					return;
				}
			}
			
			PlayerData info = knownPlayers.get(name);
			boolean op;
			if(info != null) {
				GameCore.debug("Player '"+name+"' is known, loading stored data.");
				// TODO passwords
				// if(!info.passhash.equals(passhash))
				// 	return null; // incorrect password
				player = (ServerPlayer) ServerEntity.deserialize(world, info.data, GameCore.VERSION);
				op = info.op;
			} else {
				GameCore.debug("Player '"+name+"' is not known, creating new player.");
				player = new ServerPlayer(world, name);
				// if(GameCore.debug)
				// 	op = connection.getRemoteAddressTCP().getAddress().isLoopbackAddress();
				// else
					op = isHost;
			}
			
			pinfo = new PlayerLink(connection, player, op);
			connectionToPlayerInfoMap.put(connection, pinfo);
			playerToConnectionMap.put(player, connection);
			// }
			
			connection.sendTCP(world.getWorldUpdate());
			world.postRunnable(() -> {
				if(info != null)
					world.loadLevel(info.levelId, player);
				else
					world.respawnPlayer(player);
				
				System.out.println("Server: new player successfully connected: "+player.getName());
				if(info == null) {
					synchronized (dataLock) {
						knownPlayers.put(name, new PlayerData(name, "", player.serialize(), player.getSpawnLevel(), op));
					}
				}
				
				pinfo.validationTimer.start();
				
				broadcast(new Message(player.getName()+" joined the server.", STATUS_MSG_COLOR), false, player);
			});
			
			return;
		}
		
		PlayerLink clientData = getPlayerInfo(connection);
		if(clientData == null) {
			System.err.println("server received packet from unknown client "+connection.getRemoteAddressTCP().getHostString()+"; ignoring packet "+object);
			return;
		}
		
		ServerPlayer client = clientData.player;
		
		GameProtocol.forPacket(object, Ping.class, ping -> {
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
		
		if(object.equals(DatalessRequest.Tile)) {
			Level level = client.getLevel();
			if(level != null) {
				Tile t = level.getTile(client.getInteractionRect());
				connection.sendTCP(new Message(client+" looking at "+(t==null?null:t.toLocString()), GameCore.DEFAULT_CHAT_COLOR));
			}
		}
		
		client.handlePlayerPackets(object, connection);
		
		forPacket(object, Message.class, true, msg -> {
			//System.out.println("server: executing command "+msg.msg);
			CommandInputParser.executeCommand(world, msg.msg, client, clientData.toClientOut, clientData.toClientErr);
			InfoMessage output = clientData.toClientOut.flushMessage();
			if(output != null)
				connection.sendTCP(output);
		});
		
		GameProtocol.forPacket(object, TabRequest.class, request -> {
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
			connection.sendTCP(player.getSpawnData());
			for(Entity e: level.getEntities())
				connection.sendTCP(new EntityAddition(e));
		});
	}
	
	/*public boolean isInventoryMode(@NotNull ServerPlayer player) {
		PlayerLink info = getPlayerInfo(player);
		return info != null && info.inventoryMode;
	}*/
	
	public boolean isAdmin(@Nullable ServerPlayer player) {
		if(player == null) return true; // from server command prompt
		PlayerLink info = getPlayerInfo(player);
		if(info == null) return false; // unrecognized player (should never happen)
		return info.op;
	}
	
	public boolean setAdmin(@NotNull ServerPlayer player, boolean op) {
		PlayerLink info = getPlayerInfo(player);
		if(info == null) return false; // unrecognized player (should never happen)
		info.op = op;
		world.postRunnable(() -> updatePlayerData(player));
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
	
	private void sendEntityValidation(@NotNull PlayerLink pData) {
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
		Collection<PlayerLink> data;
		synchronized (playerLock) {
			data = new ArrayList<>(connectionToPlayerInfoMap.values());
		}
		for(PlayerLink pd: data) {
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
