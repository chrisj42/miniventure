package miniventure.game.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.util.function.MapFunction;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.WorldManager;

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
	private final HashMap<Connection, PacketPipe> connectionToPipeMap = new HashMap<>();
	private final Object connectionMapLock = new Object();
	
	private final MapFunction<InetSocketAddress, Boolean> hostChecker;
	
	public NetworkServer(@NotNull WorldManager world, int port, MapFunction<InetSocketAddress, Boolean> hostChecker, PlayerData[] playerData) throws IOException {
		super(world, true, playerData);
		
		this.port = port;
		this.hostChecker = hostChecker;
		
		server = new MiniventureServer(serverWriteBufferSize, objectBufferSize);
		GameProtocol.registerClasses(server.getKryo());
		server.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if(object instanceof KeepAlive)
					return; // we don't care about these, they are internal packets
				
				world.postRunnable(() -> {
					if(object instanceof Login)
						handleLogin(connection, (Login) object);
					else {
						PacketPipeWriter packetWriter = getSendPipe(connection);
						
						if(packetWriter == null) {
							System.err.println("received packet before login, ignoring content. Client Address: "+connection.getRemoteAddressTCP().getHostString()+"; packet type: "+object.getClass().getSimpleName()+"; packet data: "+object);
							return;
						}
						
						handlePacket(packetWriter, object);
					}
				});
			}
			
			@Override
			public void disconnected(Connection connection) {
				world.postRunnable(() -> onDisconnect(connection));
			}
		});
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
	
	private void handleLogin(Connection connection, Login login) {
		MyUtils.debug("server received login");
		
		if(login.version.compareTo(Version.CURRENT) != 0) {
			connection.sendTCP(new LoginFailure("Required version: "+ Version.CURRENT));
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
		
		// here, split off into an update thread operation
		
		// for(Player p: getPlayers()) {
			if(getPlayerByName(name) != null) {
				connection.sendTCP(new LoginFailure("A player named '"+name+"' is already logged in."));
				return;
			}
		// }
		
		// login data validated
		
		PacketPipe sendPipe = new PacketPipe("Server to "+connection.getRemoteAddressTCP().getHostString()) {
			@Override
			public String toString() {
				return "PacketPipe[remote address: "+connection.getRemoteAddressTCP().getHostString()+']';
			}
		};
		PacketPipeReader packetPasser = sendPipe.getPipeReader();
		packetPasser.setListener(connection::sendTCP);
		PacketPipeWriter packetWriter = sendPipe.getPipeWriter();
		
		// synchronized (connectionMapLock) {
			writerToConnectionMap.put(packetWriter, connection);
			connectionToPipeMap.put(connection, sendPipe);
		// }
		
		packetPasser.start();
		login(name, isHost, packetWriter);
	}
	
	private void onDisconnect(Connection connection) {
		PacketPipe sendPipe;
		// synchronized (connectionMapLock) {
			sendPipe = connectionToPipeMap.get(connection);
		// }
		if(sendPipe != null) {
			sendPipe.getPipeReader().close();
			logout(sendPipe.getPipeWriter());
			// synchronized (connectionMapLock) {
				connectionToPipeMap.remove(connection);
				writerToConnectionMap.remove(sendPipe.getPipeWriter());
			// }
		}
	}
	
	@Nullable
	private PacketPipeWriter getSendPipe(Connection connection) {
		PacketPipe pipe;
		// synchronized (connectionMapLock) {
			pipe = connectionToPipeMap.get(connection);
		// }
		return pipe == null ? null : pipe.getPipeWriter();
	}
	
	@Override @Nullable
	public InetSocketAddress getPlayerAddress(@NotNull Player player) {
		PacketPipeWriter writer = getPipeWriter(player);
		Connection c;
		// synchronized (connectionMapLock) {
			c = writerToConnectionMap.get(writer);
		// }
		return c == null ? null : c.getRemoteAddressTCP();
	}
	
	@Override
	public void stopServer() { server.stop(); }
}
