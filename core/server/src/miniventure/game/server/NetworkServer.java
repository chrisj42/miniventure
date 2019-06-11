package miniventure.game.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.network.GameProtocol;
import miniventure.game.network.PacketPipe;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.ServerPlayer;
import miniventure.game.world.file.PlayerData;
import miniventure.game.world.management.ServerWorld;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.MiniventureServer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkServer extends GameServer {
	
	private final int port;
	private MiniventureServer server;
	
	private final HashMap<PacketPipeWriter, Connection> writerToConnectionMap = new HashMap<>();
	private final HashMap<Connection, PacketPipeWriter> connectionToWriterMap = new HashMap<>();
	private final Object connectionMapLock = new Object();
	
	private final MapFunction<InetSocketAddress, Boolean> hostChecker;
	
	public NetworkServer(@NotNull ServerWorld world, int port, MapFunction<InetSocketAddress, Boolean> hostChecker, PlayerData[] playerData) throws IOException {
		super(world, true, playerData);
		
		this.port = port;
		this.hostChecker = hostChecker;
		
		server = new MiniventureServer(writeBufferSize*5, objectBufferSize);
		GameProtocol.registerClasses(server.getKryo());
		server.addListener(networkListener);
		server.start();
		server.bind(port);
	}
	
	public int getPort() { return port; }
	
	@Override
	public boolean isRunning() {
		return server.getUpdateThread() != null && server.getUpdateThread().isAlive();
	}
	
	@Override
	public boolean isHost(@NotNull InetSocketAddress address) {
		return hostChecker.get(address);
	}
	
	// public int getPort() { return port; }
	
	// this will only ever be called in one thread: the server thread. So synchronizing the connectionThreadMap isn't necessary.
	private final Listener networkListener = new Listener() {
		@Override
		public void received (Connection connection, Object object) {
			if(object instanceof KeepAlive)
				return; // we don't care about these, they are internal packets
			
			// ServerThread st = connectionThreadMap.get(connection);
			// if(st != null)
			// 	st.addPacket(object);
			// else
			// 	GameCore.error("recieved packet from unregistered connection: "+connection.getRemoteAddressTCP());
			
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
				
				for(ServerPlayer p: getPlayers()) {
					if(p.getName().equals(name)) {
						connection.sendTCP(new LoginFailure("A player named '"+name+"' is already logged in."));
						return;
					}
				}
				
				// login data validated
				
				PacketPipe sendPipe = new PacketPipe() {
					@Override
					public String toString() {
						return "PacketPipe[remote address: "+connection.getRemoteAddressTCP().getHostString()+']';
					}
				};
				sendPipe.getPipeReader().addListener(connection::sendTCP);
				PacketPipeWriter packetWriter = sendPipe.getPipeWriter();
				
				synchronized (connectionMapLock) {
					writerToConnectionMap.put(packetWriter, connection);
					connectionToWriterMap.put(connection, packetWriter);
				}
				
				login(name, isHost, packetWriter);
				return;
			}
			
			PacketPipeWriter packetWriter;
			synchronized (connectionMapLock) {
				packetWriter = connectionToWriterMap.get(connection);
			}
			
			if(packetWriter == null) {
				System.err.println("received packet before login, ignoring content. Client Address: "+connection.getRemoteAddressTCP().getHostString()+"; packet type: "+object.getClass().getSimpleName()+"; packet data: "+object);
				return;
			}
			
			handlePacket(packetWriter, object);
		}
		
		@Override
		public void disconnected(Connection connection) {
			PacketPipeWriter packetSender;
			synchronized (connectionMapLock) {
				packetSender = connectionToWriterMap.get(connection);
			}
			
			if(packetSender != null)
				logout(packetSender);
		}
	};
	
	
	@Override @Nullable
	public InetSocketAddress getPlayerAddress(@NotNull ServerPlayer player) {
		PacketPipeWriter writer = getPipeWriter(player);
		Connection c;
		synchronized (connectionMapLock) {
			c = writerToConnectionMap.get(writer);
		}
		return c == null ? null : c.getRemoteAddressTCP();
	}
	
	@Override
	public void stopServer() { server.stop(); }
}
