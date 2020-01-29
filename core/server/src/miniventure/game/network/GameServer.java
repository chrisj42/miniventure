package miniventure.game.network;

import javax.swing.Timer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

import miniventure.game.chat.InfoMessage;
import miniventure.game.chat.InfoMessageBuilder;
import miniventure.game.chat.InfoMessageLine;
import miniventure.game.chat.MessageBuilder;
import miniventure.game.chat.command.Command;
import miniventure.game.chat.command.CommandInputParser;
import miniventure.game.core.GameCore;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.util.ArrayUtils;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Point;
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
import miniventure.game.world.tile.TileStack.TileData;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.network.GameProtocol.forPacket;

public abstract class GameServer implements GameProtocol {
	
	public static final Color SERVER_CHAT_COLOR = Color.WHITE;
	public static final Color STATUS_MSG_COLOR = Color.ORANGE;
	public static final Color ERROR_CHAT_COLOR = new Color(1f, .5f, .5f, 1);
	
	// public static final String PLAYER_NAME_REGEX = "( *[a-zA-Z0-9_.-] *)+";
	
	private class PlayerLink {
		final PacketPipeWriter connection;
		@NotNull final ServerPlayer player;
		final InfoMessageBuilder toClientOut, toClientErr;
		final Timer validationTimer;
		
		boolean op;
		
		// /** @see GameProtocol.InventoryRequest */
		// private boolean inventoryMode = false; // send hotbar updates at first
		
		PlayerLink(PacketPipeWriter connection, @NotNull ServerPlayer player, boolean op) {
			this.connection = connection;
			this.player = player;
			this.op = op;
			
			toClientOut = new InfoMessageBuilder(text -> new InfoMessageLine(GameCore.DEFAULT_CHAT_COLOR, text));
			toClientErr = new InfoMessageBuilder(toClientOut, text -> new InfoMessageLine(ERROR_CHAT_COLOR, text));
			
			validationTimer = new Timer(5000, e -> player.getWorld().postRunnable(() -> sendEntityValidation(this)));
			validationTimer.setRepeats(true);
		}
	}
	
	private final Map<String, PlayerData> knownPlayers = Collections.synchronizedMap(new HashMap<>());
	
	private final Map<PacketPipeWriter, PlayerLink> connectionToPlayerInfoMap = new HashMap<>();
	private final Map<ServerPlayer, PacketPipeWriter> playerToConnectionMap = new HashMap<>();
	private final Map<String, ServerPlayer> playersByName = new HashMap<>();
	private final Object playerLock = new Object();
	
	@NotNull private final ServerWorld world;
	private boolean multiplayer;
	
	public GameServer(@NotNull ServerWorld world, boolean multiplayer, PlayerData[] playerData) {
		this.world = world;
		this.multiplayer = multiplayer;
		
		for(PlayerData info: playerData)
			knownPlayers.put(info.name, info);
	}
	
	public abstract boolean isRunning();
	
	public boolean isMultiplayer() { return multiplayer; }
	
	protected abstract boolean isHost(@NotNull InetSocketAddress address);
	
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
		
		synchronized (knownPlayers) {
			return knownPlayers.values().toArray(new PlayerData[0]);
		}
	}
	public void updatePlayerData(ServerPlayer player) {
		PlayerLink info = getPlayerInfo(player);
		if(info == null) {
			System.err.println("Player "+player+" does not have PlayerLink, cannot update data.");
			return;
		}
		PlayerData pdata = knownPlayers.get(player.getName());
		if(pdata == null) {
			System.err.println("Player "+player+" does not have PlayerData, cannot update.");
			return;
		}
		
		final String name = player.getName();
		final String data = player.serialize();
		final Level level = player.getLevel();
		// using the pdata level id is better than using the spawn level because it doesn't lose the info if an extra update request is sent after the player is removed from their level.
		final int levelid = level == null ? pdata.levelId : level.getLevelId();
		knownPlayers.put(name, new PlayerData(name, pdata.passhash, data, levelid, info.op));
	}
	
	public int getPlayerCount() {
		synchronized (playerLock) {
			return playerToConnectionMap.size();
		}
	}
	
	private PlayerLink getPlayerInfo(@Nullable ServerPlayer player) {
		if(player == null)
			return null;
		synchronized (playerLock) {
			return connectionToPlayerInfoMap.get(playerToConnectionMap.get(player));
		}
	}
	
	private PlayerLink getPlayerInfo(@NotNull PacketPipeWriter connection) {
		// synchronized (playerLock) {
			return connectionToPlayerInfoMap.get(connection);
		// }
	}
	
	PacketPipeWriter getPipeWriter(@Nullable ServerPlayer player) {
		// synchronized (playerLock) {
			return playerToConnectionMap.get(player);
		// }
	}
	
	
	void login(String name, boolean isHost, PacketPipeWriter connection) {
		ServerPlayer player;
		PlayerLink pinfo;
		
		PlayerData info = knownPlayers.get(name);
		boolean op;
		if(info != null) {
			GameCore.debug("Player '"+name+"' is known, loading stored data.");
			// TODO passwords
			// if(!info.passhash.equals(passhash))
			// 	return null; // incorrect password
			player = (ServerPlayer) ServerEntity.deserialize(world, info.data, Version.CURRENT);
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
		// synchronized (playerLock) {
			connectionToPlayerInfoMap.put(connection, pinfo);
			playerToConnectionMap.put(player, connection);
			playersByName.put(player.getName(), player);
		// }
		
		// TODO delay sending world updates; here is where we check if the player has seen the intro or not.
		
		connection.send(world.getWorldUpdate());
		// world.postRunnable(() -> {
			if(info != null)
				world.loadLevel(info.levelId, player);
			else
				world.respawnPlayer(player);
			
			System.out.println("Server: new player successfully connected: "+player.getName());
			if(info == null) {
				knownPlayers.put(name, new PlayerData(name, "", player.serialize(), player.getSpawnLevel(), op));
				world.saveWorld();
			}
			
			pinfo.validationTimer.start();
			
			broadcastGlobal(player, new Message(player.getName()+" joined the server.", STATUS_MSG_COLOR));
		// });
	}
	
	void logout(PacketPipeWriter connection) {
		PlayerLink info = getPlayerInfo(connection);
		if(info == null) return;
		info.validationTimer.stop();
		ServerPlayer player = info.player;
		// world.postRunnable(() -> {
			updatePlayerData(player);
			player.remove();
			// player.getWorld().removePlayer(player);
			// synchronized (playerLock) {
				connectionToPlayerInfoMap.remove(connection);
				playerToConnectionMap.remove(player);
				playersByName.remove(player.getName());
			// }
			broadcastGlobal(new Message(player.getName()+" left the server.", STATUS_MSG_COLOR));
			//System.out.println("server disconnected from client: " + connection.getRemoteAddressTCP().getHostString());
			
			if(player.getName().equals(HOST)) // quit server when host exits
				world.exitWorld();
		// });
	}
	
	/*private <T> void forPacket(Object packet, DatalessRequest type, boolean sync, Action response) {
		GameProtocol.forPacket(packet, type, response, sync ? world::postRunnable : null);
	}*/
	/*private <T> void forPacket(Object packet, Class<T> type, ValueAction<T> response) {
		GameProtocol.forPacket(packet, type, response);
		// response.act(type.cast(packet));
	}*/
	
	protected void handlePacket(PacketPipeWriter connection, Object object) {
		// final ServerWorld world = GameServer.this.world;
		
		PlayerLink clientData = getPlayerInfo(connection);
		if(clientData == null) {
			System.err.println("server received packet from unknown client "+connection+"; ignoring packet "+object);
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
				PacketPipeWriter toClient;
				if(ping.source.equals(client.getName()))
					// this client is the one who requested the ping
					toClient = connection;
				else {
					// another player requested a ping of this player
					// synchronized (playerLock) {
						toClient = playerToConnectionMap.get(getPlayerByName(ping.source));
					// }
				}
				
				if(toClient != null)
					toClient.send(new Message(disp, Color.SKY));
			}
		});
		
		forPacket(object, MapRequest.class, req -> connection.send(world.getMapData()));
		
		forPacket(object, LevelChange.class, change -> {
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
		
		forPacket(object, EntityRequest.class, req -> {
			Entity e = world.getEntity(req.eid);
			if(e != null && e.getLevel() == client.getLevel())
				connection.send(new EntityAddition(e));
		});
		
		/*if(object.equals(DatalessRequest.Tile)) {
			Level level = client.getLevel();
			if(level != null) {
				Tile t = level.getTile(client.getInteractionRect());
				connection.send(new Message(client+" looking at "+(t==null?null:t.toLocString()), GameCore.DEFAULT_CHAT_COLOR));
			}
		}*/
		
		client.handlePlayerPackets(object, connection);
		
		forPacket(object, Message.class, msg -> {
			//System.out.println("server: executing command "+msg.msg);
			CommandInputParser.executeCommand(world, msg.msg, client, clientData.toClientOut, clientData.toClientErr);
			InfoMessage output = clientData.toClientOut.flushMessage();
			if(output != null)
				connection.send(output);
		});
		
		GameProtocol.forPacket(object, TabRequest.class, request -> {
			String text = request.manualText;
			if(!text.contains(" ")) {
				// hasn't finished entering command name, autocomplete that
				Array<String> matches = new Array<>(String.class);
				for(Command c: Command.valuesFor(client)) {
					String name = c.name();
					if(text.length() == 0 || name.toLowerCase().contains(text.toLowerCase()))
						matches.add(name);
				}
				
				if(matches.size == 0) return;
				
				if(matches.size == 1) {
					connection.send(new TabResponse(request.manualText, matches.get(0)));
					return;
				}
				
				matches.sort(String::compareToIgnoreCase);
				
				if(request.tabIndex < 0)
					connection.send(new Message(ArrayUtils.arrayToString(matches.shrink(), ", "), GameCore.DEFAULT_CHAT_COLOR));
				else {
					// actually autocomplete
					connection.send(new TabResponse(request.manualText, matches.get(request.tabIndex % matches.size)));
				}
			}
		});
	};
	
	public Set<ServerPlayer> getPlayers() {
		// synchronized (playerToConnectionMap) {
			return new HashSet<>(playerToConnectionMap.keySet());
		// }
	}
	
	public void sendToPlayer(@NotNull ServerPlayer player, Object obj) {
		PacketPipeWriter toClient = getPipeWriter(player);
		if(toClient != null) {
			toClient.send(obj);
		}
		else
			GameCore.error("Server could not find send pipe for client "+player+" to send packet: "+obj.getClass().getSimpleName());
	}
	
	public void broadcastGlobal(Object obj) {
		// synchronized (playerLock) {
			for(PacketPipeWriter out: playerToConnectionMap.values())
				out.send(obj);
		// }
	}
	private void broadcastFilter(Object obj, Predicate<ServerPlayer> filter) {
		// synchronized (playerLock) {
			for(Entry<ServerPlayer, PacketPipeWriter> entry: playerToConnectionMap.entrySet())
				if(filter.test(entry.getKey()))
					entry.getValue().send(obj);
		// }
	}
	public void broadcastGlobal(@NotNull ServerPlayer exclude, Object obj) {
		broadcastFilter(obj, player -> player != exclude);
	}
	public void broadcastLocal(ServerLevel levelMask, Object obj) {
		if(levelMask == null)
			return;
		broadcastFilter(obj, player -> player.getLevel() == levelMask);
	}
	public void broadcastLocal(ServerLevel levelMask, @NotNull ServerEntity excludeIfPlayer, Object obj) {
		if(levelMask == null)
			return;
		/*if(!(excludeIfPlayer instanceof ServerPlayer))
			broadcastLocal(levelMask, obj);
		else
			*/broadcastFilter(obj, player -> player != excludeIfPlayer && player.getLevel() == levelMask);
	}
	// levelMask is the level a player must be on to receive this data.
	/*public void broadcast(Level levelMask, Object obj, @NotNull ServerEntity... exclude) {
		if(levelMask == null) return; // no level, no packet.
		
		Set<ServerPlayer> players = getPlayers();
		HashSet<ServerEntity> excluded = new HashSet<>(Arrays.asList(exclude));
		//noinspection SuspiciousMethodCalls
		players.removeAll(excluded);
		players.removeIf(player -> !levelMask.equals(player.getLevel()));
		
		broadcast(obj, true, players);
	}
	private void broadcast(Object obj, boolean includeGiven, @NotNull Set<ServerPlayer> players) {
		if(!includeGiven) {
			Set<ServerPlayer> playerSet = getPlayers();
			playerSet.removeAll(players);
			players = playerSet;
		}
		
		for(ServerPlayer p: players)
			sendToPlayer(p, obj);
	}*/
	
	@Nullable
	public ServerPlayer getPlayerByName(String name) {
		// synchronized (playerLock) {
			return playersByName.get(name);
		// }
	}
	
	@Nullable
	public abstract InetSocketAddress getPlayerAddress(@NotNull ServerPlayer player);
	
	public void sendLevel(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		PacketPipeWriter connection = getPipeWriter(player);
		if(connection == null) {
			System.err.println("Could not send level to player "+player+", Connection obj could not be found.");
			return;
		}
		
		// world.postRunnable(() -> {
		connection.send(new LevelInfo(level.getLevelId(), level.getWidth(), level.getHeight()));
		
		TileData[][] tileData = level.getTileData(false);
		for (int i = 0; i < tileData.length; i += ServerLevel.CHUNK_SIZE) {
			for (int j = 0; j < tileData[i].length; j += ServerLevel.CHUNK_SIZE) {
				Point offset = new Point(i, j);
				int width = Math.min(ServerLevel.CHUNK_SIZE, tileData.length - i);
				int height = Math.min(ServerLevel.CHUNK_SIZE, tileData[i].length - j);
				connection.send(new LevelChunk(offset, width, height, tileData));
				MyUtils.sleep(2);
			}
		}
		
		for(Entity e: level.getEntities())
			connection.send(new EntityAddition(e));
		connection.send(player.getSpawnData());
		// });
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
		// world.postRunnable(() -> updatePlayerData(player));
		updatePlayerData(player);
		return true;
	}
	
	public void sendMessage(@Nullable ServerPlayer sender, ServerPlayer reciever, String msg) {
		sendToPlayer(reciever, getMessage(sender, msg));
	}
	public void broadcastMessage(@Nullable ServerPlayer sender, String msg) {
		broadcastGlobal(getMessage(sender, msg));
	}
	
	public Message getMessage(@Nullable ServerPlayer sender, String msg) {
		if(sender == null) return new Message("Server: "+msg, SERVER_CHAT_COLOR);
		return new Message((multiplayer?sender.getName()+": ":"")+msg, GameCore.DEFAULT_CHAT_COLOR);
	}
	
	public void broadcastParticle(ParticleData data, WorldObject posMarker) {
		broadcastParticle(data, posMarker.getLevel(), posMarker.getCenter());
	}
	public void broadcastParticle(ParticleData data, Level level, Vector2 pos) {
		broadcastGlobal(new ParticleAddition(data, new PositionUpdate(level, pos)));
	}
	
	private void sendEntityValidation(@NotNull PlayerLink pData) {
		ServerPlayer player = pData.player;
		/*if(!pData.connection.isConnected()) {
			System.err.println("Server not sending entity validation to player "+player.getName()+" because player has disconnected; stopping validation timer.");
			pData.validationTimer.stop();
			return;
		}*/
		
		ServerLevel level = player.getLevel();
		if(level == null) {
			GameCore.debug("Server: Level of player "+player.getName()+" is null during entity validation attempt, skipping validation.");
			return;
		}
		
		// world.postRunnable(() -> {
			EntityValidation validation = new EntityValidation(level, player);
			pData.connection.send(validation);
		// });
	}
	
	public void playEntitySound(String soundName, Entity source) { playEntitySound(soundName, source, true); }
	public void playEntitySound(String soundName, Entity source, boolean broadcast) {
		String fullSoundName;
		if(source instanceof Player)
			fullSoundName = "player/"+soundName;
		//else if(source instanceof MobAi)
		//	playSound("entity/"+((MobAi)source).getType().name().toLowerCase()+"/");
		else
			fullSoundName = "entity/"+soundName;
		
		if(source instanceof ServerPlayer && !broadcast)
			playGenericSound(fullSoundName, source.getCenter(), (ServerPlayer)source);
		else
			playGenericSound(fullSoundName, source.getCenter());
	}
	public void playTileSound(String soundName, Tile tile, TileTypeEnum type) {
		//playGenericSound("tile/"+type+"/"+soundName, tile.getCenter());
		playGenericSound("tile/"+soundName, tile.getCenter());
	}
	public void playGenericSound(String soundName, Vector2 source) {
		for(ServerPlayer player: playerToConnectionMap.keySet()) {
			playGenericSound(soundName, source, player);
		}
	}
	public void playGenericSound(String soundName, Vector2 source, @NotNull ServerPlayer forPlayer) {
		if(forPlayer.getPosition().dst(source) <= GameCore.SOUND_RADIUS) {
			sendToPlayer(forPlayer, new SoundRequest(soundName));
		}
	}
	
	public void printStatus(MessageBuilder out) {
		out.println("Miniventure Version: "+ Version.CURRENT);
		out.println("Server Running: "+isRunning());
		out.println("FPS: " + world.getFPS());
		out.println("Players connected: "+playerToConnectionMap.size());
		// Collection<PlayerLink> data;
		// synchronized (playerLock) {
		// 	data = new ArrayList<>(connectionToPlayerInfoMap.values());
		// }
		for(PlayerLink pd: connectionToPlayerInfoMap.values()) {
			out.print("     Player '"+pd.player.getName()+"' ");
			if(pd.op) out.print("(admin) ");
			out.print(pd.player.getLocation());
			out.println();
		}
		if(GameCore.debug)
			out.println("Debug mode is enabled.");
	}
	
	abstract void stopServer();
	
	public void stop(boolean waitForClients) {
		stopServer();
		if(!waitForClients) return;
		while(getPlayerCount() > 0)
			MyUtils.sleep(25);
	}
}
